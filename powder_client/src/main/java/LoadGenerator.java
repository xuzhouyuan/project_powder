import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class LoadGenerator implements Runnable{
    private String serverPath;
    private List<CountDownLatch> latches;
    private String resortID;
    private String dayId;
    private int numThreads;
    private int numLifts;
    private int startSkierId, endSkierId;
    private int startMinute, endMinute;
    private boolean coolDown = false;

    private static Logger logger = LogManager.getLogger(PowderClient.class);

    public LoadGenerator(String serverPath, List<CountDownLatch> latches, int numThreads,
                         String resortID, int dayId, int numLifts, int startSkierId, int endSkierId,
                         int startMinute, int endMinute) {
        this.serverPath = serverPath;
        this.latches = latches;
        this.numThreads = numThreads;
        this.resortID = resortID;
        this.dayId = Integer.toString(dayId);
        this.numLifts = numLifts;
        this.startSkierId = startSkierId;
        this.endSkierId = endSkierId;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    public LoadGenerator(String serverPath, List<CountDownLatch> latches, int numThreads,
                         String resortID, int dayId, int numLifts, int startSkierId, int endSkierId,
                         int startMinute, int endMinute, boolean coolDown) {
        this(serverPath, latches, numThreads, resortID, dayId, numLifts,
                startSkierId, endSkierId, startMinute, endMinute);
        this.coolDown = coolDown;
    }

    @Override
    public void run() {
        int numSkiers = endSkierId - startSkierId;
        if (coolDown) {
            logger.info("Starting generate loads with LoadWorkerCoolDown");
            for (int i = 0; i < numThreads; i++) {
                // calculate skierId range for different workers
                int workerStartSkierId = startSkierId + i * (numSkiers / numThreads);
                int workerEndSkierId = startSkierId + (i + 1) * (numSkiers / numThreads);
                LoadWorkerCoolDown worker = new LoadWorkerCoolDown(latches, serverPath, resortID,
                        dayId, numLifts, workerStartSkierId, workerEndSkierId, startMinute, endMinute);
                new Thread(worker).start();
            }
        } else {
            logger.info("Starting generate loads with LoadWorker");
            for (int i = 0; i < numThreads; i++) {
                // calculate skierId range for different workers
                int workerStartSkierId = startSkierId + i * (numSkiers / numThreads);
                int workerEndSkierId = startSkierId + (i + 1) * (numSkiers / numThreads);
                LoadWorker worker = new LoadWorker(latches, serverPath, resortID,
                        dayId, numLifts, startSkierId, endSkierId, startMinute, endMinute);
                new Thread(worker).start();
            }
        }
    }
}

class LoadWorker implements Runnable {
    protected List<CountDownLatch> latches;
    protected String serverPath;
    protected Random rand;

    protected String resortId;
    protected String dayId;
    protected int numLifts;
    protected int startSkierId, endSkierId;
    protected int startMinute, endMinute;

    protected int numPOST = 100;
    protected int numGET = 5;

    protected static Logger logger = LogManager.getLogger(PowderClient.class);

    public LoadWorker(List<CountDownLatch> latches, String serverPath, String resortId, String dayId,
                      int numLifts, int startSkierId, int endSkierId, int startMinute, int endMinute) {
        this.latches = latches;
        this.serverPath = serverPath;
        this.resortId = resortId;
        this.dayId = dayId;
        this.numLifts = numLifts;
        this.startSkierId = startSkierId;
        this.endSkierId = endSkierId;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        rand = new Random();
    }

    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(serverPath);
        SkiersApi api = new SkiersApi(apiClient);
        logger.info(this.getClass().getName() + "starting to create loads.");
        generatePOSTs(api);
        generateGETs(api);
        for (int i = 0; i < latches.size(); i++) {
            latches.get(i).countDown();
        }
    }

    protected void generatePOSTs(SkiersApi api) {
        for (int i = 0; i < numPOST; i++) {
            try {
                int skierId = startSkierId + rand.nextInt(endSkierId-startSkierId);
                int liftId = rand.nextInt(numLifts) + 1;
                int minute = startMinute + rand.nextInt(endMinute-startMinute);
                LiftRide ride = packLiftRide(liftId, skierId, minute);
                ApiResponse<Void> response = api.writeNewLiftRideWithHttpInfo(ride);
                String responseCode = Integer.toString(response.getStatusCode());
                if (responseCode.startsWith("4") || responseCode.startsWith("5")) {
                    logger.error(this.getClass().getName() + " received status code " + responseCode +
                            " when calling POST writeNewLiftRide");
                }
            } catch (ApiException e) {
                System.err.println("Exception when calling writeNewLiftRide");
                e.printStackTrace();
            }
        }
    }

    protected void generateGETs(SkiersApi api) {
        for (int i = 0; i < numGET; i++) {
            try {
                int skierId = startSkierId + rand.nextInt(endSkierId-startSkierId);
                ApiResponse<SkierVertical> response = api.getSkierDayVerticalWithHttpInfo(resortId, dayId,
                        Integer.toString(skierId));
                String responseCode = Integer.toString(response.getStatusCode());
                if (responseCode.startsWith("4") || responseCode.startsWith("5")) {
                    logger.error(this.getClass().getName() + " received status code " + responseCode +
                            " when calling GET getSkierDayVertical");
                }
            } catch (ApiException e) {
                System.err.println("Exception when calling getSkierDayVertical");
                e.printStackTrace();
            }
        }
    }

    protected LiftRide packLiftRide(int liftId, int skierId, int minute) {
        LiftRide ride = new LiftRide();
        ride.setDayID(dayId);
        ride.setLiftID(Integer.toString(liftId));
        ride.setResortID(resortId);
        ride.setSkierID(Integer.toString(skierId));
        ride.setTime(Integer.toString(minute));
        return ride;
    }
}

class LoadWorkerCoolDown extends LoadWorker {

    public LoadWorkerCoolDown(List<CountDownLatch> latches, String serverPath, String resortId,
                              String dayId, int numLifts,int startSkierId, int endSkierId,
                              int startMinute, int endMinute) {
        super(latches, serverPath, resortId, dayId, numLifts, startSkierId, endSkierId, startMinute, endMinute);
        numGET = 10;
    }
}

