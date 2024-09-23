package com.starryassociates.core.repo;

import com.starryassociates.core.config.OracleConfig;
import com.starryassociates.core.model.CanInfo;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class CanInfoRepo {

    private HikariDataSource dataSource;

    public CanInfoRepo() {
        this.dataSource = OracleConfig.getDataSource();
    }

    // Retrieve all CAN_INFO records from the database
    public List<CanInfo> getAllCanInfo() throws SQLException {
        String query = "SELECT CAN_CODE, CAN_DESC, FY_BEGIN, FY_END, PROJECT_NO, ADMINCODE, IS_ACTIVE, LAST_UPDATED FROM CAN_INFO";
        List<CanInfo> canInfoList = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CanInfo canInfo = new CanInfo();
                canInfo.setCanCode(rs.getString("CAN_CODE"));
                canInfo.setCanDesc(rs.getString("CAN_DESC"));
                canInfo.setFyBegin(rs.getString("FY_BEGIN"));
                canInfo.setFyEnd(rs.getString("FY_END"));
                canInfo.setProjectNo(rs.getString("PROJECT_NO"));
                canInfo.setAdminCode(rs.getString("ADMINCODE"));
                canInfo.setIsActive(rs.getBoolean("IS_ACTIVE"));
                canInfo.setLastUpdated(rs.getTimestamp("LAST_UPDATED"));

                canInfoList.add(canInfo);
            }
        }

        return canInfoList;
    }

    // Insert CanInfo record into Oracle
    public void insertCanInfo(CanInfo canInfo) throws SQLException {
        String insertQuery = "INSERT INTO CAN_INFO (CAN_CODE, CAN_DESC, FY_BEGIN, FY_END, PROJECT_NO, ADMINCODE, IS_ACTIVE, LAST_UPDATED) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            ps.setString(1, canInfo.getCanCode());
            ps.setString(2, canInfo.getCanDesc());
            ps.setString(3, canInfo.getFyBegin());
            ps.setString(4, canInfo.getFyEnd());
            ps.setString(5, canInfo.getProjectNo());
            ps.setString(6, canInfo.getAdminCode());
            ps.setBoolean(7, canInfo.getIsActive());
            ps.setTimestamp(8, new java.sql.Timestamp(canInfo.getLastUpdated().getTime()));

            ps.executeUpdate();
        }
    }

    // Update CanInfo record in Oracle
    public void updateCanInfo(CanInfo canInfo) throws SQLException {
        String updateQuery = "UPDATE CAN_INFO SET CAN_DESC = ?, FY_BEGIN = ?, FY_END = ?, PROJECT_NO = ?, ADMINCODE = ?, IS_ACTIVE = ?, LAST_UPDATED = ? " +
                "WHERE CAN_CODE = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(updateQuery)) {

            ps.setString(1, canInfo.getCanDesc());
            ps.setString(2, canInfo.getFyBegin());
            ps.setString(3, canInfo.getFyEnd());
            ps.setString(4, canInfo.getProjectNo());
            ps.setString(5, canInfo.getAdminCode());
            ps.setBoolean(6, canInfo.getIsActive());
            ps.setTimestamp(7, new java.sql.Timestamp(canInfo.getLastUpdated().getTime()));
            ps.setString(8, canInfo.getCanCode());

            ps.executeUpdate();
        }
    }

    // Delete CanInfo record from Oracle
    public void deleteCanInfo(String canCode) throws SQLException {
        String deleteQuery = "DELETE FROM CAN_INFO WHERE CAN_CODE = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteQuery)) {

            ps.setString(1, canCode);
            ps.executeUpdate();
        }
    }
}
