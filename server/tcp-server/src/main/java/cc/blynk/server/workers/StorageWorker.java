package cc.blynk.server.workers;

import cc.blynk.server.model.enums.GraphType;
import cc.blynk.server.storage.reporting.average.AggregationKey;
import cc.blynk.server.storage.reporting.average.AggregationValue;
import cc.blynk.server.storage.reporting.average.AverageAggregator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static cc.blynk.server.storage.StorageDao.generateFilename;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 10.08.15.
 */
public class StorageWorker implements Runnable {

    private static final Logger log = LogManager.getLogger(StorageWorker.class);
    private static final Comparator<AggregationKey> AGGREGATION_KEY_COMPARATOR = (o1, o2) -> (int) (o1.ts - o2.ts);

    private final AverageAggregator averageAggregator;
    private final String dataFolder;

    public StorageWorker(AverageAggregator averageAggregator, String dataFolder) {
        this.averageAggregator = averageAggregator;
        this.dataFolder = dataFolder;
    }

    public static void write(Path reportingPath, double value, long ts) {
        try (DataOutputStream dos = new DataOutputStream(
                Files.newOutputStream(reportingPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            dos.writeDouble(value);
            dos.writeLong(ts);
            dos.flush();
        } catch (IOException e) {
            log.error("Error open user data reporting file.", e);
        }
    }

    @Override
    public void run() {
        process(averageAggregator.getHourly(), GraphType.HOURLY);
        process(averageAggregator.getDaily(), GraphType.DAILY);
    }

    private void process(Map<AggregationKey, AggregationValue> map, GraphType type) {
        long nowTruncatedToPeriod = System.currentTimeMillis() / type.period;

        List<AggregationKey> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys, AGGREGATION_KEY_COMPARATOR);


        for (AggregationKey key : keys) {
            //if prev hour
            if (key.ts < nowTruncatedToPeriod) {
                AggregationValue value = map.get(key);

                double average = value.calcAverage();
                long eventTS = key.ts * type.period;

                String fileName = generateFilename(key.dashId, key.pinType, key.pin, type);

                Path reportingPath = Paths.get(dataFolder, key.username, fileName);
                write(reportingPath, average, eventTS);
                map.remove(key);
            }
        }
    }

}
