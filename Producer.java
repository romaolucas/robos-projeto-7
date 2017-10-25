import lejos.robotics.navigation.DifferentialPilot;
import lejos.nxt.Motor;
import lejos.util.Delay;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.io.IOException;


class Producer extends Thread {
    static DifferentialPilot pilot;
    static UltrasonicSensor sonar;

    private static final byte TRAVEL = 0; 
    private static final byte ROTATE = 1;
    private static final byte FULL_SCAN = 2; 
    private static final byte SINGLE_SCAN = 3; 
    private static final byte EXIT = 4;

    private Buffer buffer;
    private Buffer original;
    private int posX = 0;
    private int posY = 0;
    private int theta = 0;

    public Producer(Buffer b) {
        buffer = b;
        original = b;
    }

    public void reinitialize() {
        buffer = original;
    }

    public void writeValuesToFile(List<Integer> readValues) {
        String values = readValues.stream().map(i -> i.toString())
            .collect(Collectors.joining(", "));
        StringBuilder sb = new StringBuilder("");
        sb.append(posX + ", ");
        sb.append(posY + ", ");
        sb.append(theta + ", ");
        sb.append(values);
        sb.append("\n");
        String line = sb.toString();
        try {
            Files.write(Paths.get("data.txt"), line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    public void run() {
        Delay.msDelay(2000);
        Scanner scan = new Scanner(System.in);
        byte cmd = 0; 
        int param = 0;
        pilot = new DifferentialPilot(5.6f, 11.2f, Motor.B, Motor.A);
        sonar = new UltrasonicSensor(SensorPort.S1);
        pilot.setTravelSpeed(10);
        pilot.setRotateSpeed(40);

        while (true) {
            System.out.print("Enter command [0:TRAVEL 1:ROTATE 2:FULL_SCAN 3:SINGLE_SCAN 4:EXIT]: ");
            cmd = (byte) scan.nextFloat(); 
            if (cmd == 0 || cmd == 1) {
                System.out.println("Enter integer parameter: ");
                param = scan.nextInt();
            }
            switch (cmd) {
                case TRAVEL: 
                    pilot.travel(param);
                    posX += (int) param*Math.cos(theta);
                    posY += (int) param*Math.sin(theta);
                    break;
                case ROTATE: 
                    pilot.rotate(param);
                    theta += param;
                    break;
                case FULL_SCAN:
                    fullscan();
                    break;  
                case SINGLE_SCAN:
                    int val = sonar.getDistance();
                    System.out.println("Sonar Single Scan(cm): " + val);
                    break;
                case EXIT:
                    System.out.println("close");
                    System.exit(0);
                default:
                    System.out.println("default");
            }
        }
    }

    public void fullscan() { 
        int val = 0;
        int i = 0;
        List<Integer> readValues = new ArrayList<>();
        double ang = 0;
        Motor.C.setSpeed(200);
        Motor.C.rotate(450);
        Motor.C.setSpeed(100);
        for (int j = 0; j <= 900; j += 10) {
            Motor.C.rotate(-10);
            val = sonar.getDistance();
            ang = j / 900.0 * 180.0;
            this.buffer.put((int) ang, val);
            readValues.add(val);
        }
        Motor.C.setSpeed(200);
        Motor.C.rotate(460);
        reinitialize();
        writeValuesToFile(readValues);
    }

}
