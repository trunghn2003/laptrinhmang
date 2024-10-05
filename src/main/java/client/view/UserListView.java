package client.view;

import server.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class UserListView extends JFrame {
    private JTable userTable;

    public UserListView(List<User> users) {
        setTitle("User List");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        String[] columnNames = {"ID", "Username", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);


        for (User user : users) {
            Object[] rowData = {user.getId(), user.getUserName(), user.getScore(), user.getStatus()};
            tableModel.addRow(rowData);
        }


        userTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane);


        setLocationRelativeTo(null);
        setVisible(true);
    }

}
