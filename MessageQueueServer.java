import java.util.*;
import java.util.concurrent.*;

/**
 * Simple message queue server implementation
 */
public class MessageQueueServer {
    // Message queues for different topics
    private Map<String, BlockingQueue<Message>> queues;
    private Map<String, List<MessageConsumer>> subscribers;
    private ExecutorService executorService;
    private volatile boolean running;
    
    public MessageQueueServer() {
        this.queues = new ConcurrentHashMap<>();
        this.subscribers = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.running = true;
        
        initializeQueues();
    }
    
    /**
     * Initialize message queues for different topics
     */
    private void initializeQueues() {
        createQueue("payment-requests");
        createQueue("payment-responses");
        createQueue("stock-updates");
        createQueue("reorder-requests");
        createQueue("loyalty-updates");
        createQueue("transaction-logs");
    }
    
    /**
     * Create a new message queue
     */
    public void createQueue(String queueName) {
        queues.put(queueName, new LinkedBlockingQueue<>());
        subscribers.put(queueName, new CopyOnWriteArrayList<>());
        System.out.println("Created queue: " + queueName);
    }
    
    /**
     * Send message to a queue (point-to-point)
     */
    public void sendMessage(String queueName, Message message) {
        BlockingQueue<Message> queue = queues.get(queueName);
        if (queue != null) {
            try {
                queue.offer(message, 5, TimeUnit.SECONDS);
                System.out.println("Message sent to queue " + queueName + ": " + message.getContent());
            } catch (InterruptedException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
    }
    
    /**
     * Publish message to subscribers (publish-subscribe)
     */
    public void publishMessage(String topic, Message message) {
        List<MessageConsumer> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            for (MessageConsumer consumer : topicSubscribers) {
                executorService.execute(() -> {
                    try {
                        consumer.onMessage(message);
                    } catch (Exception e) {
                        System.err.println("Error delivering message to subscriber: " + e.getMessage());
                    }
                });
            }
            System.out.println("Published message to " + topicSubscribers.size() + 
                             " subscribers on topic: " + topic);
        }
    }
    
    /**
     * Subscribe to a topic
     */
    public void subscribe(String topic, MessageConsumer consumer) {
        List<MessageConsumer> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            topicSubscribers.add(consumer);
            System.out.println("New subscriber added to topic: " + topic);
        }
    }
    
    /**
     * Receive message from queue (blocking)
     */
    public Message receiveMessage(String queueName, long timeout, TimeUnit unit) 
            throws InterruptedException {
        BlockingQueue<Message> queue = queues.get(queueName);
        if (queue != null) {
            return queue.poll(timeout, unit);
        }
        return null;
    }
    
    /**
     * Start the message queue server
     */
    public void start() {
        System.out.println("Message Queue Server started");
        
        // Start queue monitoring thread
        executorService.execute(() -> {
            while (running) {
                try {
                    Thread.sleep(10000); // Monitor every 10 seconds
                    printQueueStatus();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    /**
     * Print queue status
     */
    private void printQueueStatus() {
        System.out.println("\n--- Queue Status ---");
        for (Map.Entry<String, BlockingQueue<Message>> entry : queues.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().size() + " messages");
        }
    }
    
    /**
     * Shutdown the server
     */
    public void shutdown() {
        running = false;
        executorService.shutdown();
        System.out.println("Message Queue Server shutdown");
    }
    
    public static void main(String[] args) {
        MessageQueueServer server = new MessageQueueServer();
        server.start();
        
        // Keep server running
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}
