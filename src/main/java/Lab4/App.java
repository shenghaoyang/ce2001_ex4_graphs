package Lab4;

import picocli.CommandLine;

public class App {
    public static void main(String[] args) {
        System.exit(new CommandLine(new BenchmarkCommand()).execute(args));
    }
}
