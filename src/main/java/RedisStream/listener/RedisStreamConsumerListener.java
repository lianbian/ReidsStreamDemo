package RedisStream.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class RedisStreamConsumerListener implements StreamListener<String, MapRecord<String ,String, String>> {
    static final Logger logger = LoggerFactory.getLogger(RedisStreamConsumerListener.class);

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        logger.info("message:: id={}, body={}", message.getId(), message.getValue());
    }
}
