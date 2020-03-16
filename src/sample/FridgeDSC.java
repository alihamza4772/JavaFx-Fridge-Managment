package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class FridgeDSC implements Initializable {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement preparedStatement;
    private boolean deletecheck = false;
    @FXML
    private ComboBox filterbybox, selectbox_add, sectionbox;
    @FXML
    private Label itemlabel_add, sectionlabel_add, quantitylabel_add, updateqtylabel;
    @FXML
    private TableColumn<Grocery, String> id, item, quantity, section, bought;
    @FXML
    private TextField filterbox, quantitytextbox, updateqtytext;
    @FXML
    private Button savebutton, cancelbutton, updatebutton, canclebuttonupdate;
    @FXML
    private CheckBox expire_checkbox;
    @FXML
    private TableView<Grocery> Home_table;
    private URL url = null;
    private ResourceBundle resourceBundle = null;
    private ObservableList<Grocery> tableData;

    public static void connect() throws SQLException                 // function for database connection
    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/fridgeassignment";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public static void disconnect() throws SQLException                   // function for database disconnection
    {
        if (preparedStatement != null) preparedStatement.close();
        if (statement != null) statement.close();
        if (connection != null) connection.close();
    }

    public static long calcDaysAgo(LocalDate date) throws ParseException                // func to convert date into days ago
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        return Math.abs(Duration.between(LocalDate.now().atStartOfDay(), date.atStartOfDay()).toDays());
    }

    public static String calcDaysAgoStr(String date) throws ParseException                           // func to convert date into days ago string
    {
        String formattedDaysAgo;
        LocalDate ndate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT));
        long diff = calcDaysAgo(ndate);
        if (diff == 0)
            formattedDaysAgo = "today";
        else if (diff == 1)
            formattedDaysAgo = "yesterday";
        else formattedDaysAgo = diff + " days ago";

        return formattedDaysAgo;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)           // Initializer to set all the things on startup
    {
        hideall();
        expire_checkbox.disableProperty().setValue(true);
        setfilterboxvalues();
        filltablevalues();
    }

    public void AddButton_click(ActionEvent actionEvent) throws Exception        // Add Button Click listener
    {
        showall();
        ObservableList<String> sectionlist = FXCollections.observableArrayList(SECTION.FREEZER.name(), SECTION.MEAT.name(), SECTION.COOLING.name(), SECTION.CRISPER.name());
        sectionbox.setItems(sectionlist);
        setuplistvalues();
    }

    public void CANCEL_CLICK_ADD(ActionEvent actionEvent)                         // Cancle Button Click listener
    {
        itemlabel_add.setVisible(false);
        selectbox_add.setVisible(false);
        sectionlabel_add.setVisible(false);
        sectionbox.setVisible(false);
        quantitytextbox.setVisible(false);
        quantitylabel_add.setVisible(false);
        savebutton.setVisible(false);
        cancelbutton.setVisible(false);
    }

    public void filterby_onaction(ActionEvent actionEvent)                 // filterby combobox click listener
    {
        if (filterbybox.getSelectionModel().getSelectedItem().toString() == "BOUGHT_DAYS_AGO") {
            expire_checkbox.setDisable(false);
        } else
            expire_checkbox.setDisable(true);
    }

    public void deletebutton_click(ActionEvent actionEvent) throws Exception       // Delete Button Click listener
    {
        if (deletecheck == true) {
            int id = Home_table.getSelectionModel().getSelectedItem().getId();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Delete the Entire Row.!");
            alert.setContentText("Are you ok with this?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                int check = removeGrocery(id);
                if (check == id) {
                    Home_table.getItems().removeAll(Home_table.getSelectionModel().getSelectedItem());
                    Alert_Success("Data Deleted Successfuly.!");
                    deletecheck = false;
                }
            } else {
                return;
            }
        } else if (deletecheck == false) {
            Alert_Error("If You Want to delete Any Data Kindly Select that Row.!");
        }
    }

    public void table_click(MouseEvent mouseEvent) {
        deletecheck = true;
        updateqtytext.setText("");
        updateqtytext.setText(String.valueOf(Home_table.getSelectionModel().getSelectedItem().getQuantity()));
    }

    public void savebtn_click(ActionEvent actionEvent) throws Exception                       // save button Click listener
    {
        if (quantitytextbox.getText().length() == 0 || selectbox_add.getSelectionModel().isEmpty() == true || sectionbox.getSelectionModel().isEmpty() == true) {
            Alert_Error("Choose Values from the Both Combobox and Enter Quantity in Textbox.!");
        } else {
            int check = addGrocery(getnamefromstr(selectbox_add.getSelectionModel().getSelectedItem().toString()), Integer.valueOf(quantitytextbox.getText()), sectionbox.getSelectionModel().getSelectedItem().toString());
            if (check != 0) {
                Alert_Success("Data Added Successfully.!");
                initialize(url, resourceBundle);
            } else {
                Alert_Error("Data Didn't Added Successfully.!");
            }
        }
    }

    public void filtertextbox(ActionEvent actionEvent) throws Exception              // function for searching item
    {
        searchGrocery();
    }

    public void updatebtn_click(ActionEvent actionEvent) {
        updateqtylabel.setVisible(true);
        updateqtytext.setVisible(true);
        updatebutton.setVisible(true);
        canclebuttonupdate.setVisible(true);

    }

    public void CANCEL_CLICK_UPDATE(ActionEvent actionEvent) {
        hideall();
    }

    public void updatebuttonclick(ActionEvent actionEvent) throws Exception {
        UpdateGrocery(Home_table.getSelectionModel().getSelectedItem().getId());
        Alert_Success("Updated Successfully.!");
        initialize(url, resourceBundle);
    }

    public void expire_onaction(ActionEvent actionEvent) throws Exception {
        if (expire_checkbox.isSelected() == true) {
            ObservableList<Grocery> list = expiredItemslist();
            SortedList<Grocery> sortedList = new SortedList<>(list);
            sortedList.comparatorProperty().bind(Home_table.comparatorProperty());
            Home_table.setItems(sortedList);
        } else {
            initialize(url, resourceBundle);
            System.out.println("Done");
        }
    }

    public void searchGrocery() throws Exception {
        FilteredList<Grocery> searchdata = new FilteredList<>(tableData);
        filterbox.textProperty().addListener((observable) -> {
            searchdata.setPredicate(Grocery -> {

                if (filterbox.getText().toString() == null || filterbox.getText().isEmpty()) {
                    return true;
                }
                String low = filterbox.getText().toLowerCase();
                if (filterbybox.getValue().toString() == FridgeFX.FILTER_COLUMNS.ITEM.name()) {
                    if (Grocery.getItemName().toLowerCase().indexOf(low) != -1) {
                        return true;
                    } else return false;
                } else if (filterbybox.getValue().toString() == FridgeFX.FILTER_COLUMNS.SECTION.name()) {
                    if (Grocery.getSection().toLowerCase().indexOf(low) != -1) {
                        return true;
                    } else return false;
                } else
                    return false;
            });
        });

        SortedList<Grocery> sortedList = new SortedList<>(searchdata);
        sortedList.comparatorProperty().bind(Home_table.comparatorProperty());
        Home_table.setItems(sortedList);
    }

    public ObservableList<Item> getAllItems() throws Exception {

        String queryString = "SELECT * FROM item";
        ObservableList<Item> Itemlist = FXCollections.observableArrayList();
        ResultSet rs = statement.executeQuery(queryString);
        while (rs.next()) {
            Itemlist.add(new Item(rs.getString(1), rs.getBoolean(2)));
        }

        return Itemlist;
    }

    public ObservableList<Grocery> getAllGroceries() throws Exception {
        String queryString = "SELECT * FROM grocery";
        ObservableList<Grocery> groceries = FXCollections.observableArrayList();
        ResultSet rs = statement.executeQuery(queryString);
        while (rs.next()) {
            groceries.add(new Grocery(rs.getInt(1), new Item((String) rs.getObject(2)), calcDaysAgoStr(rs.getString(3)), rs.getInt(4), rs.getString(5)));
        }
        return groceries;
    }

    public int addGrocery(String name, int quantity, String section) throws Exception          // Add Grocery func
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDate date = LocalDate.now();
        String dateStr = date.format(dtf);
        String query = "INSERT INTO grocery(itemName,date,quantity,section) values('" + name + "','" + dateStr + "'," + quantity + ",'" + section + "')";
        statement = connection.createStatement();
        statement.executeUpdate(query);
        ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID()");
        rs.next();
        int checkid = rs.getInt(1);
        return checkid;
    }

    public Grocery UpdateGrocery(int id) throws Exception                    // Update Grocery func
    {
        String queryString =
                "UPDATE grocery " +
                        "SET quantity = " + updateqtytext.getText() + " " +
                        "WHERE quantity > 1 " +
                        "AND id = " + id + ";";
        Statement statement = connection.createStatement();
        statement.executeUpdate(queryString);
        return null;
    }

    public int removeGrocery(int id) throws Exception                             // Delete Grocery func
    {
        String queryString = "DELETE FROM grocery WHERE id = " + id + "";
        statement = connection.createStatement();
        statement.executeUpdate(queryString);
        return id;
    }

    public void hideall() {
        itemlabel_add.setVisible(false);
        selectbox_add.setVisible(false);
        sectionlabel_add.setVisible(false);
        sectionbox.setVisible(false);
        selectbox_add.getSelectionModel().clearSelection();
        sectionbox.getSelectionModel().clearSelection();
        quantitytextbox.setVisible(false);
        quantitylabel_add.setVisible(false);
        savebutton.setVisible(false);
        cancelbutton.setVisible(false);
        updateqtylabel.setVisible(false);
        updateqtytext.setVisible(false);
        updatebutton.setVisible(false);
        canclebuttonupdate.setVisible(false);

    }

    public void filltablevalues()                                // function for polulate the values into your tableview
    {
        tableData = FXCollections.observableArrayList();
        try {
            tableData = getAllGroceries();
        } catch (Exception e) {
            e.printStackTrace();
        }

        id.setCellValueFactory(new PropertyValueFactory<Grocery, String>("id"));
        item.setCellValueFactory(new PropertyValueFactory<Grocery, String>("Item"));
        quantity.setCellValueFactory(new PropertyValueFactory<Grocery, String>("quantity"));
        section.setCellValueFactory(new PropertyValueFactory<Grocery, String>("Section"));
        bought.setCellValueFactory(new PropertyValueFactory<Grocery, String>("date"));
        Home_table.setItems(null);
        Home_table.setItems(tableData);
    }

    public void setfilterboxvalues()                              // function for set all the values from enum to filterby combobox
    {
        filterbybox.setValue(FridgeFX.FILTER_COLUMNS.ITEM.name());
        ObservableList<String> filterlist = FXCollections.observableArrayList(FridgeFX.FILTER_COLUMNS.ITEM.name(), FridgeFX.FILTER_COLUMNS.SECTION.name(), FridgeFX.FILTER_COLUMNS.BOUGHT_DAYS_AGO.name());
        filterbybox.setItems(filterlist);
    }

    public void Alert_Error(String text)                        // function for error alert
    {
        Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
        alert1.setTitle("Error Dialog");
        alert1.setHeaderText(null);
        alert1.setContentText(text);
        alert1.show();
    }

    public void Alert_Success(String msg)                     // function for Success alert
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success Dialog");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }

    public void showall() {
        itemlabel_add.setVisible(true);
        selectbox_add.setVisible(true);
        sectionlabel_add.setVisible(true);
        sectionbox.setVisible(true);
        quantitytextbox.setVisible(true);
        quantitylabel_add.setVisible(true);
        savebutton.setVisible(true);
        cancelbutton.setVisible(true);
    }

    public void setuplistvalues() throws Exception                // function to setup Item list on Add menu
    {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < getAllItems().toArray().length; i++) {
            list.add((getAllItems().get(i).todetailString()));
        }
        ObservableList<String> list1 = FXCollections.observableArrayList(list);
        selectbox_add.setItems(list1);
    }

    public String getnamefromstr(String text)                     // conversion function
    {
        String[] arr = text.split(",");
        text = arr[0];
        String[] arrnew = text.split(": ");
        text = arrnew[1];
        return text;
    }

    public ObservableList<Grocery> expiredItemslist() throws Exception              // function for expire grocery items
    {
        ObservableList<Grocery> newlist = FXCollections.observableArrayList();
        ArrayList<Grocery> glocery = getGroceries();
        ArrayList<Item> item = getItems();
        for (int i = 0; i < glocery.size(); i++) {
            for (int j = 0; j < item.size(); j++) {
                if (glocery.get(i).getItemName().equals(item.get(j).getName()) && item.get(j).canExpire() == true) {
                    newlist.add(new Grocery(glocery.get(i).getId(), new Item(glocery.get(i).getItemName()), calcDaysAgoStr(glocery.get(i).getDate()), glocery.get(i).getQuantity(), glocery.get(i).getSection()));
                }
            }
        }
        return newlist;
    }

    public ArrayList<Grocery> getGroceries() throws Exception               // func for fetching Grocery data
    {
        String queryString = "SELECT * FROM grocery";
        ArrayList<Grocery> groceries = new ArrayList<>();
        ResultSet rs = statement.executeQuery(queryString);
        while (rs.next()) {
            groceries.add(new Grocery(rs.getInt(1), new Item((String) rs.getObject(2)), rs.getString(3), rs.getInt(4), rs.getString(5)));
        }
        return groceries;
    }

    public ArrayList<Item> getItems() throws Exception                      // func for fetching Items data
    {
        String queryString = "SELECT * FROM item";
        ArrayList<Item> Itemlist = new ArrayList<>();
        ResultSet rs = statement.executeQuery(queryString);
        while (rs.next()) {
            Itemlist.add(new Item(rs.getString(1), rs.getBoolean(2)));
        }
        return Itemlist;
    }

    public enum SECTION {
        FREEZER,
        MEAT,
        COOLING,
        CRISPER
    }
}

