package RedisStream.config;

import RedisStream.listener.RedisStreamConsumerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.util.ErrorHandler;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RedisStreamConfig {
    private String streamKey = "lianbianKey";

    @Autowired
    private RedisStreamConsumerListener streamListener;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public StreamMessageListenerContainer streamMessageListenerContainer() {
        AtomicInteger index = new AtomicInteger(1);
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processors, processors, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), r -> {
            Thread thread = new Thread(r);
            thread.setName("async-stream-comsumer-" + index.getAndDecrement());
            thread.setDaemon(true);
            return thread;
        });

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder().batchSize(10).executor(executor).errorHandler(new ErrorHandler() {
            @Override
            public void handleError(Throwable t) {
                t.printStackTrace();
            }
        }).pollTimeout(Duration.ZERO).serializer(new StringRedisSerializer()).build();
        StreamMessageListenerContainer streamMessageListenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, streamMessageListenerContainerOptions);
        streamMessageListenerContainer.receive(Consumer.from("lianbianGroup", "consumer-a"), StreamOffset.create("lianbianKey", ReadOffset.lastConsumed()), streamListener);
        streamMessageListenerContainer.start();
        return streamMessageListenerContainer;
    }
}
