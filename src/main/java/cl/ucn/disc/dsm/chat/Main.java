/*
 * Copyright (c) 2019. This code is purely educational, the rights of use are
 * reserved, the owner of the code is Alvaro Castillo Calabacero,
 * contact alvaro.castillo@alumnos.ucn.cl
 * Do not use in production.
 */

package cl.ucn.disc.dsm.chat;
import java.io.IOException;

/**
 * @author Alvaro Castillo
 */
public class Main {
    /**
     * port to start the server
     */
    private static final int PORT = 9000;
    /**
     *Principal
     */
    public static void main(final String[] args) throws IOException {
        ChatServer Server = new ChatServer(PORT); //initialize the server and put it to run
        Server.start(); //start de server
    }
}
