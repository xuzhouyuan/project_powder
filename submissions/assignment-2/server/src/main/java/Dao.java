import io.swagger.client.model.LiftRide;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Dao {
    protected static BasicDataSource dataSource;
    protected static final int maxConnection = 600;

    public Dao() {
       dataSource = DBCPDataSource.getDataSource();
       dataSource.setMaxTotal(maxConnection);
    }
}

class LiftRideDao extends Dao {
    public LiftRideDao() {
        super();
    }

    public void writeNewLiftRide(LiftRide ride) throws SQLException, ConnectionLeakException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "INSERT INTO LiftRides " +
                "(skierID, dayID, time, resortID, liftID) " +
                "VALUES (?,?,?,?,?)";
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setString(1, ride.getSkierID());
            preparedStatement.setString(2, ride.getDayID());
            preparedStatement.setString(3, ride.getTime());
            preparedStatement.setString(4, ride.getResortID());
            preparedStatement.setString(5, ride.getLiftID());

            // execute insert SQL statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        if (!conn.isClosed()) {
            throw new ConnectionLeakException();
        }
        return;
    }

    public int getSkierDayVertical(String resortID, String dayID, String skierID) throws SQLException, ConnectionLeakException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet;
        int totalVert;
        String insertQueryStatement = "SELECT SUM(l.vertical) FROM LiftRides r LEFT JOIN Lifts l " +
                "ON (r.resortID = l.resortID AND r.liftID = l.liftID) " +
                "WHERE (r.skierID = ? AND r.dayID = ? AND r.resortID = ?)";

        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setString(1, skierID);
            preparedStatement.setString(2, dayID);
            preparedStatement.setString(3, resortID);

            // execute insert SQL statement
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            totalVert = resultSet.getInt("SUM(l.vertical)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        if (!conn.isClosed()) {
            throw new ConnectionLeakException();
        }
        return totalVert;
    }

    public int getSkierResortTotals(String skierID, String resortID) throws SQLException, ConnectionLeakException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet;
        int totalVert;
        String insertQueryStatement = "SELECT SUM(l.vertical) FROM LiftRides r LEFT JOIN Lifts l " +
                "ON (r.resortID = l.resortID AND r.liftID = l.liftID) " +
                "WHERE (r.skierID = ? AND r.resortID = ?)";


        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setString(1, skierID);
            preparedStatement.setString(2, resortID);

            // execute insert SQL statement
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            totalVert = resultSet.getInt("SUM(l.vertical)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        if (!conn.isClosed()) {
            throw new ConnectionLeakException();
        }
        return totalVert;
    }

    public int getTopTenVert() {
        // TODO, future

        return 1;
    }
}


    // "SELECT SUM(l.vertical) FROM LiftRides r LEFT JOIN Lifts l ON (r.resortID = l.resortID AND r.liftID = l.liftID) WHERE (r.skierID = skierID AND r.dayID = dayID AND r.resortID = resortID);"

