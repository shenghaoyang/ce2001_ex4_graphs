package Lab4;

import Lab4.Benchmark.BenchmarkCommand;
import picocli.CommandLine;

public class App {
    public static void main(String[] args) {
        System.exit(new CommandLine(new BenchmarkCommand()).execute(args));
    }
}
