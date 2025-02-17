package cc.blynk.server.dao;

import cc.blynk.server.dao.graph.GraphInMemoryStorage;
import cc.blynk.server.dao.graph.GraphKey;
import cc.blynk.server.dao.graph.StoreMessage;
import cc.blynk.server.model.Profile;
import cc.blynk.server.model.auth.User;
import cc.blynk.server.model.enums.PinType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/26/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class GraphInMemoryStorageDaoTest {

    private GraphInMemoryStorage storage = new GraphInMemoryStorage(1000);

    @Mock
    private User user;

    @Mock
    private Profile profile;

    @Test
    public void testStoreCorrect() throws InterruptedException {
        when(user.getProfile()).thenReturn(profile);
        GraphKey key = new GraphKey(1, (byte) 33, PinType.ANALOG);
        when(profile.hasGraphPin(key)).thenReturn(true);

        for (int i = 0; i < 1000; i++) {
            storage.store(new StoreMessage(new GraphKey(1, (byte) 33, PinType.ANALOG), String.valueOf(i), System.currentTimeMillis()));
        }

        Queue<StoreMessage> queue;
        while ((queue = storage.getAll(key)) == null || queue.size() < 1000) {
            Thread.sleep(10);
        }

        assertEquals(1000, queue.size());

        int i = 0;
        for (StoreMessage value : queue) {
            String expectedBody = String.valueOf(i++);
            assertEquals(expectedBody, value.value);
        }
    }

    @Test
    public void testStoreCorrect2() throws InterruptedException {
        when(user.getProfile()).thenReturn(profile);
        GraphKey key = new GraphKey(1, (byte) 33, PinType.ANALOG);
        when(profile.hasGraphPin(key)).thenReturn(true);

        for (int i = 0; i < 2000; i++) {
            storage.store(new StoreMessage(new GraphKey(1, (byte) 33, PinType.ANALOG), String.valueOf(i), System.currentTimeMillis()));
        }

        Queue<StoreMessage> queue;
        while ((queue = storage.getAll(key)) == null || queue.size() < 1000) {
            Thread.sleep(10);
        }

        assertEquals(1000, queue.size());

        //todo fix
        /*
        int i = 1000;
        for (StoreMessage value : queue) {
            String expectedBody = body.replace("x", String.valueOf(i++));
            assertEquals(expectedBody, value.body);
        }
        */
    }

    @Test
    public void testStoreCorrect3() throws InterruptedException {
        when(user.getProfile()).thenReturn(profile);
        GraphKey key = new GraphKey(1, (byte) 33, PinType.ANALOG);
        GraphKey key2 = new GraphKey(1, (byte) 34, PinType.ANALOG);
        when(profile.hasGraphPin(key)).thenReturn(true);
        when(profile.hasGraphPin(key2)).thenReturn(true);

        for (int i = 0; i < 1000; i++) {
            storage.store(new StoreMessage(new GraphKey(1, (byte) 33, PinType.ANALOG), String.valueOf(i), System.currentTimeMillis()));
            storage.store(new StoreMessage(new GraphKey(1, (byte) 34, PinType.ANALOG), String.valueOf(i), System.currentTimeMillis()));
        }


        Queue<StoreMessage> tmp;
        while ((tmp = storage.getAll(key)) == null || tmp.size() < 1000) {
            Thread.sleep(10);
        }

        Queue<StoreMessage> queue = storage.getAll(key);
        assertNotNull(queue);
        assertEquals(1000, queue.size());

        Queue<StoreMessage> queue2 = storage.getAll(key);
        assertNotNull(queue2);
        assertEquals(1000, queue2.size());


        int i = 0;
        for (StoreMessage value : queue) {
            String expectedBody = String.valueOf(i++);
            assertEquals(expectedBody, value.value);
        }

        i = 0;
        for (StoreMessage value : queue2) {
            String expectedBody = String.valueOf(i++);
            assertEquals(expectedBody, value.value);
        }

    }

}
