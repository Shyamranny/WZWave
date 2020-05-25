package com.whizzosoftware.wzwave.console;

import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.netty.NettyZWaveController;

import java.io.File;

public class WZWaveConsoleMain {

    /**
     * The main method.
     *
     * @param args the command arguments
     */
    public static void main(final String[] args) {

        ZWaveController zWaveController = new NettyZWaveController("/dev/tty.GoControl_zwave", new File("./storage"));

        final WZWaveConsole console = new WZWaveConsole(zWaveController);

        console.start();

        System.out.println("Console closed.");
    }
}
