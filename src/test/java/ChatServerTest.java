/*
 * Copyright (c) 2019. This code is purely educational, the rights of use are
 * reserved, the owner of the code is Alvaro Castillo Calabacero,
 * contact alvaro.castillo@alumnos.ucn.cl
 * Do not use in production.
 */
import cl.ucn.disc.dsm.chat.ChatServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alvaro Castillo
 */
public class ChatServerTest {
    /**
     * test for body content from socket
     * @throws IOException
     */
    @Test
    public void testImputs() throws IOException {
        ChatServer chatServer = new ChatServer(9000);
        List<String> list = new ArrayList<String>();
        //first case
        list.add("username=&message=");
        Assert.assertFalse(chatServer.addToDB(list));
        list.clear();
        //second case
        list.add("username=yo&message=");
        Assert.assertFalse(chatServer.addToDB(list));
        list.clear();
        //third case
        list.add("username=&message=yo");
        Assert.assertFalse(chatServer.addToDB(list));
        list.clear();
        //fourth case
        list.add("username=yo&message=mensaje");
        Assert.assertTrue(chatServer.addToDB(list));
    }
}
