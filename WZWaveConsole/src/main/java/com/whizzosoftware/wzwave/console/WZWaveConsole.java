package com.whizzosoftware.wzwave.console;

import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A console class useful to execute commands.
 */
public class WZWaveConsole {

    private final ZWaveController zWaveController;
    /**
     * The main thread.
     */
    private Thread mainThread = null;

    /**
     * The flag reflecting that shutdown is in process.
     */
    private boolean shutdown = false;

    /**
     * Map of registered commands and their implementations.
     */
    private Map<String, ConsoleCommand> commands = new TreeMap<String, ConsoleCommand>();

    private long initialMemory;

    /**
     * Constructor
     * @param zWaveController
     */
    public WZWaveConsole(final ZWaveController zWaveController) {
        this.zWaveController = zWaveController;

        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        initialMemory = runtime.totalMemory() - runtime.freeMemory();

        commands.put("quit", new QuitCommand());
        commands.put("help", new HelpCommand());
        commands.put("start", new NetStartCommand());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown = true;
                try {
                    System.in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mainThread.interrupt();
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    /**
     * Prints line to console.
     *
     * @param line the line
     */
    private static void print(final String line, final PrintStream out) {
        out.println("\r" + line);
        // if (out == System.out) {
        // System.out.print("\r> ");
        // }
    }

    public void start() {
        mainThread = Thread.currentThread();
        System.out.print("WZWaveConsole API starting up...");

        print("WZWaveConsole console ready.", System.out);

        String inputLine;
        while (!shutdown && (inputLine = readLine()) != null) {
            processInputLine(inputLine, System.out);
        }

    }

    /**
     * Reads line from console.
     *
     * @return line readLine from console or null if exception occurred.
     */
    private String readLine() {
        System.out.print("\r> ");
        try {
            final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            return bufferRead.readLine();
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Processes text input line.
     *
     * @param inputLine the input line
     * @param out the output stream
     */
    public void processInputLine(final String inputLine, final PrintStream out) {
        if (inputLine.length() == 0) {
            return;
        }
        final String[] args = inputLine.replaceAll("\\s+", " ").split(" ");
        processArgs(args, out);
    }

    /**
     * Processes input arguments.
     *
     * @param args the input arguments
     * @param out the output stream
     */
    public void processArgs(final String[] args, final PrintStream out) {
        try {
            // if (commands.containsKey(args[0])) {
            executeCommand(zWaveController, args, out);
            // } else {
            // print("Uknown command. Use 'help' command to list available commands.", out);
            // }
        } catch (final Exception e) {
            print("Exception in command execution: ", out);
            e.printStackTrace(out);
        }
    }

    /**
     * Executes command.
     *
     * @param zWaveController the {@link ZWaveController}
     * @param args the arguments including the command
     * @param out the output stream
     */
    private void executeCommand(final ZWaveController zWaveController, final String[] args, final PrintStream out) {
        final ConsoleCommand consoleCommand = commands.get(args[0].toLowerCase());
        if (consoleCommand != null) {
            try {
                consoleCommand.process(zWaveController, args, out);
            } catch (Exception e) {
                out.println("Error executing command: " + e);
                e.printStackTrace(out);
            }
            return;
        }

        print("Uknown command. Use 'help' command to list available commands.", out);
    }


    /**
     * Interface for console commands.
     */
    private interface ConsoleCommand {
        /**
         * Get command description.
         *
         * @return the command description
         */
        String getDescription();

        /**
         * Get command syntax.
         *
         * @return the command syntax
         */
        String getSyntax();

        /**
         * Processes console command.
         *
         * @param zWaveController the ZWaveController
         * @param args the command arguments
         * @param out the output PrintStream
         * @return true if command syntax was correct.
         */
        boolean process(final ZWaveController zWaveController, final String[] args, PrintStream out) throws Exception;
    }

    /**
     * Quits console.
     */
    private class QuitCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "Quits console.";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "quit";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final ZWaveController zWaveController, final String[] args, PrintStream out) {
            shutdown = true;
            return true;
        }
    }

    /**
     * Prints help on console.
     */
    private class HelpCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "View command help.";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "help [command]";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final ZWaveController zWaveController, final String[] args, PrintStream out) {

            if (args.length == 2) {
                if (commands.containsKey(args[1])) {
                    final ConsoleCommand command = commands.get(args[1]);
                    print(command.getDescription(), out);
                    print("", out);
                    print("Syntax: " + command.getSyntax(), out);
                } else {
                    return false;
                }
            } else if (args.length == 1) {
                final List<String> commandList = new ArrayList<String>(commands.keySet());
                Collections.sort(commandList);
                print("Commands:", out);
                for (final String command : commands.keySet()) {
                    print(command + " - " + commands.get(command).getDescription(), out);
                }

            } else {
                return false;
            }

            return true;
        }
    }

    private class NetStartCommand implements ConsoleCommand {

        @Override
        public String getDescription() {
            return "Start the network";
        }

        @Override
        public String getSyntax() {
            return "netstart";
        }

        @Override
        public boolean process(ZWaveController zWaveController, String[] args, PrintStream out) throws Exception {

            zWaveController.start();
            return true;
        }
    }

    private ZWaveControllerListener zWaveControllerListener = new ZWaveControllerListener() {
        @Override
        public void onZWaveNodeAdded(ZWaveEndpoint node) {

            print("onZWaveNodeAdded: " + node, System.out);
        }

        @Override
        public void onZWaveNodeUpdated(ZWaveEndpoint node) {
            print("onZWaveNodeUpdated: " + node, System.out);
        }

        @Override
        public void onZWaveConnectionFailure(Throwable t) {
            print("onZWaveConnectionFailure", System.err);
            t.printStackTrace(System.err);
        }

        @Override
        public void onZWaveControllerInfo(String libraryVersion, Integer homeId, Byte nodeId) {
            print("onZWaveControllerInfo - libraryVersion:" + libraryVersion + ", homeId:" + homeId + ", nodeId:" + nodeId, System.out);
        }

        @Override
        public void onZWaveInclusionStarted() {
            print("onZWaveInclusionStarted", System.out);
        }

        @Override
        public void onZWaveInclusion(NodeInfo nodeInfo, boolean success) {
            print("onZWaveInclusion, nodeInfo:" + nodeInfo + ", success:" + success, System.out);
        }

        @Override
        public void onZWaveInclusionStopped() {
            print("onZWaveInclusionStopped", System.out);
        }

        @Override
        public void onZWaveExclusionStarted() {
            print("onZWaveExclusionStarted", System.out);
        }

        @Override
        public void onZWaveExclusion(NodeInfo nodeInfo, boolean success) {
            print("onZWaveExclusion - nodeInfo:" + nodeInfo + ", success:" + success, System.out);
        }

        @Override
        public void onZWaveExclusionStopped() {
            print("onZWaveExclusionStopped", System.out);
        }
    };
}
