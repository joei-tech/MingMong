package florante.mingmong;

import java.sql.*;

public class JDBC {

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "system";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static class PlayerData {
        public int playerId;
        public String username;
        public int wins;
        public int losses;
        public int highestScore;

        public PlayerData(int id, String name, int wins, int losses, int highestScore) {
            this.playerId = id;
            this.username = name;
            this.wins = wins;
            this.losses = losses;
            this.highestScore = highestScore;
        }
    }

    public static PlayerData getPlayer(String username) throws SQLException {
        String sql = "SELECT * FROM players WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new PlayerData(
                        rs.getInt("player_id"),
                        rs.getString("username"),
                        rs.getInt("wins"),
                        rs.getInt("losses"),
                        rs.getInt("highest_score")
                );
            }
        }
    }

    public static boolean addPlayer(String username) throws SQLException {
        String sql = "INSERT INTO players (username, wins, losses, highest_score) "
                + "VALUES (?, 0, 0, 0)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updatePlayer(int playerId, String newUsername)
            throws SQLException {

        String sql = "UPDATE players SET username = ? WHERE player_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newUsername);
            ps.setInt(2, playerId);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deletePlayer(int playerId) throws SQLException {
        String sql = "DELETE FROM players WHERE player_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, playerId);
            return ps.executeUpdate() > 0;
        }
    }

    public static int createMatch(int player1Id, int player2Id)
            throws SQLException {

        String sql = "INSERT INTO matches (player_1_id, player_2_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"MATCH_ID"})) {

            ps.setInt(1, player1Id);
            ps.setInt(2, player2Id);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Could not create match.");
    }

    public static void saveRound(
            int matchId, int roundNumber, int player1Score, int player2Score)
            throws SQLException {

        String sql = "INSERT INTO rounds "
                + "(match_id, round_number, player_1_score, player_2_score) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, matchId);
            ps.setInt(2, roundNumber);
            ps.setInt(3, player1Score);
            ps.setInt(4, player2Score);
            ps.executeUpdate();
        }
    }

    public static void finishMatch(
            int matchId, int winnerId, int loserId,
            int winnerScore, int loserScore) throws SQLException {

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                String sql = "UPDATE matches SET winner_id = ? WHERE match_id = ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, winnerId);
                    ps.setInt(2, matchId);
                    ps.executeUpdate();
                }

                updatePlayerStats(conn, winnerId, 1, 0, winnerScore);
                updatePlayerStats(conn, loserId, 0, 1, loserScore);

                conn.commit();

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void updatePlayerStats(
            Connection conn, int playerId,
            int wins, int losses, int highestScore) throws SQLException {

        String sql = "UPDATE players SET "
                + "wins = wins + ?, "
                + "losses = losses + ?, "
                + "highest_score = GREATEST(highest_score, ?) "
                + "WHERE player_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wins);
            ps.setInt(2, losses);
            ps.setInt(3, highestScore);
            ps.setInt(4, playerId);
            ps.executeUpdate();
        }
    }
}
