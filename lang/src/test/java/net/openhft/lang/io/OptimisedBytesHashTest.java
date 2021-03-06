/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.io;

import org.junit.Ignore;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by peter on 28/06/15.
 */
public class OptimisedBytesHashTest {

    /*
   @Test
    public void testApplyAsLong() {
        Bytes b = DirectStore.allocate(128).bytes();
        b.writeLong(0x0102030405060708L);
        b.writeLong(0x1112131415161718L);
        b.flip();
        while (b.remaining() > 0) {
            assertEquals("Rem: " + b.remaining(),
                    VanillaBytesStoreHash.INSTANCE.hash(b),
                    OptimisedBytesHash.INSTANCE.hash(b));
            b.readSkip(1);
        }
        assertEquals(VanillaBytesStoreHash.INSTANCE.hash(b),
                OptimisedBytesHash.INSTANCE.hash(b));
    }
*/
/*
    @Test
    public void sizeMatch() {
        Bytes nb = DirectStore.allocate(128).bytes();
        for (int i = 1; i <= 64; i++)
            nb.writeUnsignedByte(i);
        nb.flip();
        assertEquals(0L, applyAsLong1to7(nb, 0));
        for (int i = 1; i <= 7; i++)
            assertEquals(applyAsLong1to7(nb, i), applyAsLong9to16(nb, i));
        assertEquals(applyAsLong8(nb), applyAsLong1to7(nb, 8));
        assertEquals(applyAsLong8(nb), applyAsLong9to16(nb, 8));
        for (int i = 1; i <= 16; i++)
            assertEquals(applyAsLong9to16(nb, i), applyAsLong17to32(nb, i));
        for (int i = 1; i <= 32; i++)
            assertEquals(applyAsLong17to32(nb, i), applyAsLongAny(nb, i));
    }
*/
    @Test
    @Ignore("Long running, avg score = 6879")
    public void testRandomness() {
        long time = 0, timeCount = 0;
        long scoreSum = 0;
        for (int t = 0; t < 500; t++) {
            long[] hashs = new long[8192];
            byte[] init = new byte[hashs.length / 8];
            Bytes b = DirectStore.allocate(init.length).bytes();
            new SecureRandom().nextBytes(init);
            for (int i = 0; i < hashs.length; i++) {
                b.clear();
                b.write(init);

                b.writeLong(i >> 6 << 3, 1L << i);

                b.position(0);
                b.limit(init.length);
                long start = System.nanoTime();
                hashs[i] = VanillaBytesHash.INSTANCE.hash(b);

                time += System.nanoTime() - start;
                timeCount++;
            }
            long score = 0;
            for (int i = 0; i < hashs.length - 1; i++)
                for (int j = i + 1; j < hashs.length; j++) {
                    long diff = hashs[j] ^ hashs[i];
                    int diffBC = Long.bitCount(diff);
                    if (diffBC <= 17) {
                        long d = 1L << (17 - diffBC);
                        score += d;
                    }
                }
            scoreSum += score;
            if (t % 50 == 0)
                System.out.println(t + " - Score: " + score);
        }
        System.out.println("Average score: " + scoreSum / 500);
        System.out.printf("Average time %.3f us%n", time / timeCount / 1e3);
    }

    @Test
    @Ignore("Long running, avg score = 1594788, note lower is better")
    public void testRandomnessOld() {
        long time = 0, timeCount = 0;
        long scoreSum = 0;
        for (int t = 0; t < 500; t++) {
            long[] hashs = new long[8192];
            byte[] init = new byte[hashs.length / 8];
            Bytes b = DirectStore.allocate(init.length).bytes();
            new SecureRandom().nextBytes(init);
            for (int i = 0; i < hashs.length; i++) {
                b.clear();
                b.write(init);

                b.writeLong(i >> 6 << 3, 1L << i);

                b.position(0);
                b.limit(init.length);
                long start = System.nanoTime();
                hashs[i] = VanillaBytesHasher.INSTANCE.hash(b);

                time += System.nanoTime() - start;
                timeCount++;
            }
            long score = 0;
            for (int i = 0; i < hashs.length - 1; i++)
                for (int j = i + 1; j < hashs.length; j++) {
                    long diff = hashs[j] ^ hashs[i];
                    int diffBC = Long.bitCount(diff);
                    if (diffBC <= 17) {
                        long d = 1L << (17 - diffBC);
                        score += d;
                    }
                }
            scoreSum += score;
            if (t % 50 == 0)
                System.out.println(t + " - Score: " + score);
        }
        System.out.println("Average score: " + scoreSum / 500);
        System.out.printf("Average time %.3f us%n", time / timeCount / 1e3);
    }

    @Test
    @Ignore("Long running, avg score = 6823, avg time 0.027 us")
    public void testSmallRandomness() {
        long time = 0, timeCount = 0;
        long scoreSum = 0;
//        StringBuilder sb = new StringBuilder();

        for (int t = 0; t < 500; t++) {
            long[] hashs = new long[8192];
            Bytes b = DirectStore.allocate(hashs.length / 64).bytes();
            for (int i = 0; i < hashs.length; i++) {
                b.clear();
                b.append(t);
                b.append('-');
                b.append(i);
                long start = System.nanoTime();
                hashs[i] = VanillaBytesHash.INSTANCE.hash(b);
                time += System.nanoTime() - start;
                timeCount++;

/*               if (true) {
                    sb.setLength(0);
                    sb.append(b);
                    assertEquals(hashs[i], Maths.longHash(sb));
                }*/
            }
            long score = 0;
            for (int i = 0; i < hashs.length - 1; i++)
                for (int j = i + 1; j < hashs.length; j++) {
                    long diff = hashs[j] ^ hashs[i];
                    int diffBC = Long.bitCount(diff);
                    if (diffBC < 18) {
                        long d = 1L << (17 - diffBC);
                        score += d;
                    }
                }
            scoreSum += score;
            if (t % 50 == 0)
                System.out.println(t + " - Score: " + score);
        }
        System.out.println("Average score: " + scoreSum / 500);
        System.out.printf("Average time %.3f us%n", time / timeCount / 1e3);
    }

    @Test
    @Ignore("Only run for comparison, avg score = 6843")
    public void testSecureRandomness() {
        long scoreSum = 0;
        for (int t = 0; t < 500; t++) {
            Random rand = new SecureRandom();
            long[] hashs = new long[8192];
            for (int i = 0; i < hashs.length; i++) {
                hashs[i] = rand.nextLong();
            }
            int score = 0;
            for (int i = 0; i < hashs.length - 1; i++)
                for (int j = i + 1; j < hashs.length; j++) {
                    long diff = hashs[j] ^ hashs[i];
                    int diffBC = Long.bitCount(diff);
                    if (diffBC < 18) {
                        int d = 1 << (17 - diffBC);
                        score += d;
                    }
                }
            scoreSum += score;
            if (t % 50 == 0)
                System.out.println(t + " - Score: " + score);
        }
        System.out.println("Average score: " + scoreSum / 500);
    }

}