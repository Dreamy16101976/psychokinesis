import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

class Mon extends Thread {
    @Override
    public void run() {
        for (;;) {
            int ones;
            int zeros;
            ones = Rng.ones;
            zeros = Rng.zeros;
            if ((ones + zeros) > 0) {
                System.out.print("1:" + ones + " ");
                System.out.println("0:" + zeros);
                System.out.print("1:" + 100.0 * ones / (ones + zeros) + "% ");
                System.out.println("0:" + 100.0 * zeros / (ones + zeros) + "%");
            }
            try {
                sleep(10000);
            } catch (Exception e) {
            }
        }
    }
}

public class Rng {
    public static int ones = 0;
    public static int zeros = 0;

    public static void main(String[] args) {
        System.out.println("True RNG");
        AudioFormat format = new AudioFormat(44100.0f, 8, 1, false, true);
        TargetDataLine line;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Sound capture not supported!");
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int numBytesRead;
            byte[] data = new byte[line.getBufferSize()];
            int bufSize = (line.getBufferSize() / 4) * 2;
            Mon mon = new Mon();
            mon.start();
            line.start();
            short THRES = 50;// threshold level
            boolean pulse = false;
            for (;;) {
                try {
                    numBytesRead = line.read(data, 0, bufSize);
                    for (int i = 0; i < numBytesRead - 1; i++) {
                        // System.out.println(data[i]);
                        if (pulse == false) {
                            if (Math.abs(data[i]) >= THRES) {
                                pulse = true;
                                if ((i & 1) == 1) {
                                    ones++;
                                } else {
                                    zeros++;
                                }
                            }
                        } else {
                            if (Math.abs(data[i]) < THRES) {
                                pulse = false;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

}