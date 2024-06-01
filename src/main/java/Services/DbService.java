package Services;

import Models.Appointment;
import Models.Client;
import Models.Doctor;
import Models.Insurance;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DbService {
    private static DbService instance;
    private Connection connection;

    private DbService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dbname", "username", "password");
            System.out.println("Database connection established.");
        } catch (Exception e) {
            System.out.println("Error establishing database connection: " + e.getMessage());
        }
    }

    public static DbService getInstance() {
        if (instance == null) {
            instance = new DbService();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void addClientToDatabase(Connection connection, Client client) {
        PreparedStatement p = null;
        if (connection == null) {
            System.out.println("Connection is null. Cannot add client to database.");
            return;
        }

        try {
            Long insuranceId = null;
            if (client.getInsurance() != null) {
                String insertInsuranceSQL = "INSERT INTO INSURANCE (type, exp_date) VALUES (?, ?)";
                p = connection.prepareStatement(insertInsuranceSQL, Statement.RETURN_GENERATED_KEYS);
                p.setString(1, client.getInsurance().getType());
                p.setString(2, client.getInsurance().getExpDate());
                p.executeUpdate();
                AuditService.logAction("CreateClient");


                ResultSet generatedKeys = p.getGeneratedKeys();
                if (generatedKeys.next()) {
                    insuranceId = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Failed to retrieve insurance ID.");
                }
                //p.close();
            }

            String insertClientSQL = "INSERT INTO CLIENT (name, age, phone_number, insurance_id) VALUES (?, ?, ?, ?)";
            p = connection.prepareStatement(insertClientSQL);
            p.setString(1, client.getName());
            p.setInt(2, client.getAge());
            p.setString(3, client.getPhoneNumber());

            if (insuranceId != null) {
                p.setLong(4, insuranceId);
            } else {
                p.setNull(4, Types.BIGINT);
            }
            p.executeUpdate();
            AuditService.logAction("CreateInsurance");

        } catch (SQLException e) {
            System.out.println("Error adding client to database: " + e.getMessage());
        } finally {
            try {
                if (p != null) p.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void viewInfo(Connection connection, String clientName) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot retrieve client information.");
            return;
        }

        Client client = findClientByName(connection, clientName);

        if (client == null) {
            System.out.println("Client not found.");
            return;
        }

        System.out.println("Client Information:");
        System.out.println("Name: " + client.getName());
        System.out.println("Age: " + client.getAge());
        System.out.println("Phone Number: " + client.getPhoneNumber());
        AuditService.logAction("ReadClient");


        Insurance insurance = client.getInsurance();

        if (insurance != null) {
            System.out.println("Insurance Information:");
            System.out.println("Type: " + insurance.getType());
            System.out.println("Date: " + insurance.getExpDate());
            AuditService.logAction("ReadInsurance");

        } else {
            System.out.println("No insurance information.");
        }
    }

    public Client findClientByName(Connection connection, String clientName) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot search client in database.");
            return null;
        }

        String searchClientSQL = "SELECT c.*, i.TYPE as insurance_type, i.EXP_DATE as insurance_exp_date " +
                "FROM CLIENT c " +
                "LEFT JOIN INSURANCE i ON c.INSURANCE_ID = i.id " +
                "WHERE c.NAME = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(searchClientSQL)) {
            preparedStatement.setString(1, clientName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String phoneNumber = resultSet.getString("phone_number");

                Insurance insurance = null;
                if (resultSet.getString("insurance_type") != null) {
                    String insuranceType = resultSet.getString("insurance_type");
                    String insuranceExpDate = resultSet.getString("insurance_exp_date");
                    insurance = new Insurance(insuranceType, insuranceExpDate);
                    insurance.setId(resultSet.getInt("INSURANCE_ID"));
                }

                Client client = new Client(name, age, phoneNumber, insurance);
                client.setId(id);
                return client;
            }
        } catch (SQLException e) {
            System.out.println("Error finding client: " + e.getMessage());
        }
        return null;
    }
    public Client findClientById(Connection connection, Long id) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot search client in database.");
            return null;
        }

        String searchClientSQL = "SELECT * FROM CLIENT WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(searchClientSQL)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Client client = new Client();
                client.setId(resultSet.getLong("id"));
                client.setName(resultSet.getString("name"));
                client.setAge(resultSet.getInt("age"));
                client.setPhoneNumber(resultSet.getString("phone_number"));

                Long insuranceId = resultSet.getLong("insurance_id");
                if (insuranceId != null) {
                    Insurance insurance = findInsuranceById(connection, insuranceId);
                    client.setInsurance(insurance);
                }

                return client;
            }
        } catch (SQLException e) {
            System.out.println("Error finding client: " + e.getMessage());
        }
        return null;
    }

    private Insurance findInsuranceById(Connection connection, Long id) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot search insurance in database.");
            return null;
        }

        String searchInsuranceSQL = "SELECT * FROM INSURANCE WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(searchInsuranceSQL)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Insurance insurance = new Insurance();
                insurance.setId(resultSet.getInt("id"));
                insurance.setType(resultSet.getString("type"));
                insurance.setExpDate(resultSet.getString("exp_date"));
                return insurance;
            }
        } catch (SQLException e) {
            System.out.println("Error finding insurance: " + e.getMessage());
        }
        return null;
    }

    public void editClient(Connection connection, String clientName) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot edit client.");
            return;
        }

        try {
            Client client = findClientByName(connection, clientName);

            if (client == null) {
                System.out.println("Client not found.");
                return;
            }
            AuditService.logAction("UpdateClient");


            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter new name (leave blank to keep current): ");
            String newName = scanner.nextLine();
            if (newName.isEmpty()) {
                newName = client.getName();
            }

            System.out.println("Enter new age (leave blank to keep current): ");
            String newAgeInput = scanner.nextLine();
            Integer newAge = newAgeInput.isEmpty() ? client.getAge() : Integer.parseInt(newAgeInput);

            System.out.println("Enter new phone number (leave blank to keep current): ");
            String newPhoneNumber = scanner.nextLine();
            if (newPhoneNumber.isEmpty()) {
                newPhoneNumber = client.getPhoneNumber();
            }

            String updateClientSQL = "UPDATE CLIENT SET NAME = ?, AGE = ?, PHONE_NUMBER = ? WHERE id = ?";
            try (PreparedStatement updateClientStatement = connection.prepareStatement(updateClientSQL)) {
                updateClientStatement.setString(1, newName);
                updateClientStatement.setInt(2, newAge);
                updateClientStatement.setString(3, newPhoneNumber);
                updateClientStatement.setLong(4, client.getId());
                updateClientStatement.executeUpdate();
                System.out.println("Client information updated.");
            }

            Insurance insurance = client.getInsurance();
            if (insurance != null) {
                System.out.println("Enter new insurance type (leave blank to keep current): ");
                String newInsuranceType = scanner.nextLine();
                if (newInsuranceType.isEmpty()) {
                    newInsuranceType = insurance.getType();
                }

                System.out.println("Enter new insurance expiration date (leave blank to keep current): ");
                String newInsuranceExpDate = scanner.nextLine();
                if (newInsuranceExpDate.isEmpty()) {
                    newInsuranceExpDate = insurance.getExpDate();
                }

                String updateInsuranceSQL = "UPDATE INSURANCE SET TYPE = ?, EXP_DATE = ? WHERE id = ?";
                try (PreparedStatement updateInsuranceStatement = connection.prepareStatement(updateInsuranceSQL)) {
                    updateInsuranceStatement.setString(1, newInsuranceType);
                    updateInsuranceStatement.setString(2, newInsuranceExpDate);
                    updateInsuranceStatement.setInt(3, insurance.getId());
                    updateInsuranceStatement.executeUpdate();
                    AuditService.logAction("UpdateInsurance");

                    System.out.println("Insurance information updated.");
                }
            } else {
                System.out.println("Client does not have an insurance to update.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating client: " + e.getMessage());
        }
    }




    public void removeClient(Connection connection, String clientName) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot remove client.");
            return;
        }

        try {
            Client client = findClientByName(connection, clientName);

            if (client == null) {
                System.out.println("Client not found.");
                return;
            }

            String selectAppointmentsSQL = "SELECT * FROM APPOINTMENT WHERE client_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectAppointmentsSQL)) {
                preparedStatement.setLong(1, client.getId());
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    long appointmentId = resultSet.getLong("id");
                    removeAppointment(connection, appointmentId);
                }

                String deleteClientSQL = "DELETE FROM CLIENT WHERE id = ?";
                try (PreparedStatement deleteClientStatement = connection.prepareStatement(deleteClientSQL)) {
                    deleteClientStatement.setLong(1, client.getId());
                    deleteClientStatement.executeUpdate();
                    AuditService.logAction("DeleteClient");

                    System.out.println("Client removed successfully.");
                }

                if (client.getInsurance() != null) {
                    String deleteInsuranceSQL = "DELETE FROM INSURANCE WHERE id = ?";
                    try (PreparedStatement deleteInsuranceStatement = connection.prepareStatement(deleteInsuranceSQL)) {
                        deleteInsuranceStatement.setLong(1, client.getInsurance().getId());
                        deleteInsuranceStatement.executeUpdate();
                        AuditService.logAction("DeleteInsurance");
                        System.out.println("Insurance removed successfully.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error removing client: " + e.getMessage());
        }
    }
    private void removeAppointment(Connection connection, long appointmentId) throws SQLException {
        String deleteAppointmentSQL = "DELETE FROM APPOINTMENT WHERE id = ?";
        try (PreparedStatement deleteAppointmentStatement = connection.prepareStatement(deleteAppointmentSQL)) {
            deleteAppointmentStatement.setLong(1, appointmentId);
            deleteAppointmentStatement.executeUpdate();
            AuditService.logAction("DeleteAppointment");
            System.out.println("Any related appointments were removed.");
        }
    }




    public List<Client> getAllClients(Connection connection) {
        List<Client> clients = new ArrayList<>();

        if (connection == null) {
            System.out.println("Connection is null. Cannot retrieve clients.");
            return clients;
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String selectAllClientsSQL = "SELECT * FROM CLIENT";
            preparedStatement = connection.prepareStatement(selectAllClientsSQL);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String phoneNumber = resultSet.getString("phone_number");

                Client client = new Client(name, age, phoneNumber, null);
                clients.add(client);

            }
        } catch (SQLException e) {
            System.out.println("Error retrieving clients from the database: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        AuditService.logAction("ReadClient");
        return clients;
    }

    public void addDoctors(Connection connection, Doctor doctor) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot add doctor to database.");
            return;
        }

        PreparedStatement p = null;
        try {
            String insertDoctorSQL = "INSERT INTO DOCTOR (name, phone, specialization, employment_date) VALUES (?, ?, ?, ?)";
            p = connection.prepareStatement(insertDoctorSQL);
            p.setString(1, doctor.getName());
            p.setString(2, doctor.getPhone());
            p.setString(3, doctor.getSpecialization());
            p.setObject(4, doctor.getEmploymentDate());
            p.executeUpdate();
            AuditService.logAction("CreateDoctor");

        } catch (SQLException e) {
            System.out.println("Error adding doctor to database: " + e.getMessage());
        } finally {
            try {
                if (p != null) p.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Doctor findDoctorByName(Connection connection, String name) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot search doctor in database.");
            return null;
        }

        String searchDoctorSQL = "SELECT * FROM DOCTOR WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(searchDoctorSQL)) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Doctor doctor = new Doctor();
                doctor.setId(resultSet.getLong("id"));
                doctor.setName(resultSet.getString("name"));
                doctor.setPhone(resultSet.getString("phone"));
                doctor.setSpecialization(resultSet.getString("specialization"));
                doctor.setEmploymentDate(resultSet.getObject("employment_date", LocalDateTime.class));
                return doctor;
            }
        } catch (SQLException e) {
            System.out.println("Error finding doctor: " + e.getMessage());
        }
        return null;
    }

    public void editDoctor(Connection connection, String doctorName) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot edit doctor.");
            return;
        }

        try {
            Doctor doctor = findDoctorByName(connection, doctorName);

            if (doctor == null) {
                System.out.println("Doctor not found.");
                return;
            }

            Scanner scanner = new Scanner(System.in);

            System.out.println("Enter new name (leave blank to keep current): ");
            String newName = scanner.nextLine();
            if (newName.isEmpty()) {
                newName = doctor.getName();
            }

            System.out.println("Enter new phone number (leave blank to keep current): ");
            String newPhoneNumber = scanner.nextLine();
            if (newPhoneNumber.isEmpty()) {
                newPhoneNumber = doctor.getPhone();
            }

            System.out.println("Enter new specialization (leave blank to keep current): ");
            String newSpecialization = scanner.nextLine();
            if (newSpecialization.isEmpty()) {
                newSpecialization = doctor.getSpecialization();
            }

            System.out.println("Enter new employment date (yyyy-mm-dd, leave blank to keep current): ");
            String newEmploymentDateInput = scanner.nextLine();
            LocalDateTime newEmploymentDate = newEmploymentDateInput.isEmpty() ? doctor.getEmploymentDate() : LocalDateTime.parse(newEmploymentDateInput);

            String updateDoctorSQL = "UPDATE DOCTOR SET NAME = ?, PHONE = ?, SPECIALIZATION = ?, EMPLOYMENT_DATE = ? WHERE id = ?";
            try (PreparedStatement updateDoctorStatement = connection.prepareStatement(updateDoctorSQL)) {
                updateDoctorStatement.setString(1, newName);
                updateDoctorStatement.setString(2, newPhoneNumber);
                updateDoctorStatement.setString(3, newSpecialization);
                updateDoctorStatement.setObject(4, newEmploymentDate);
                updateDoctorStatement.setLong(5, doctor.getId());
                updateDoctorStatement.executeUpdate();
                AuditService.logAction("UpdateDoctor");
                System.out.println("Doctor information updated successfully.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating doctor: " + e.getMessage());
        }
    }


    public Doctor findDoctorById(Connection connection, Long id) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot search doctor in database.");
            return null;
        }

        String searchDoctorSQL = "SELECT * FROM DOCTOR WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(searchDoctorSQL)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Doctor doctor = new Doctor();
                doctor.setId(id);
                doctor.setName(resultSet.getString("name"));
                doctor.setPhone(resultSet.getString("phone"));
                doctor.setSpecialization(resultSet.getString("specialization"));
                doctor.setEmploymentDate(resultSet.getObject("employment_date", LocalDateTime.class));
                return doctor;
            }
        } catch (SQLException e) {
            System.out.println("Error finding doctor: " + e.getMessage());
        }
        return null;
    }


    public void removeDoc(Connection connection, String docName) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot remove client.");
            return;
        }

        Doctor doc = findDoctorByName(connection, docName);

        if (doc == null) {
            System.out.println("Doctor not found.");
            return;
        }

        try {
            String deleteDocSQL = "DELETE FROM DOCTOR WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteDocSQL)) {
                preparedStatement.setLong(1, doc.getId());
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    AuditService.logAction("DeleteDoctor");
                    System.out.println("Doctor removed successfully.");
                } else {
                    System.out.println("Failed to remove doctor.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error removing doctor: " + e.getMessage());
        }
    }

    public List<Doctor> getAllDoctors(Connection connection) {
        List<Doctor> doctors = new ArrayList<>();

        if (connection == null) {
            System.out.println("Connection is null. Cannot retrieve doctors.");
            return doctors;
        }

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String selectAllDoctorsSQL = "SELECT * FROM DOCTOR";
            preparedStatement = connection.prepareStatement(selectAllDoctorsSQL);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                String specialization = resultSet.getString("specialization");
                LocalDateTime employmentDate = resultSet.getTimestamp("employment_date").toLocalDateTime();

                Doctor doctor = new Doctor(name, phone, specialization, employmentDate);
                doctors.add(doctor);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving doctors from the database: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        AuditService.logAction("ReadDoctor");
        return doctors;
    }

    public void addAppointment(Connection connection, String clientName, String doctorName, LocalDateTime appointmentDate) {
        if (connection == null) {
            System.out.println("Connection is null. Cannot add appointment to database.");
            return;
        }

        Client client = findClientByName(connection, clientName);
        Doctor doctor = findDoctorByName(connection, doctorName);

        if (client == null) {
            System.out.println("Client not found.");
            return;
        }

        if (doctor == null) {
            System.out.println("Doctor not found.");
            return;
        }

        try {
            String insertAppointmentSQL = "INSERT INTO APPOINTMENT (date, doctor_id, client_id) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertAppointmentSQL)) {
                preparedStatement.setObject(1, appointmentDate);
                preparedStatement.setLong(2, doctor.getId());
                preparedStatement.setLong(3, client.getId());
                preparedStatement.executeUpdate();
                AuditService.logAction("CreateAppointment");

                System.out.println("Appointment added successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error adding appointment to database: " + e.getMessage());
        }
    }

    public List<Appointment> getAppointmentsForClientByName(Connection connection, String clientName) {
        List<Appointment> appointments = new ArrayList<>();

        if (connection == null) {
            System.out.println("Connection is null. Cannot retrieve appointments.");
            return appointments;
        }

        try {
            Client client = findClientByName(connection, clientName);

            if (client == null) {
                System.out.println("Client not found.");
                return appointments;
            }

            String selectAppointmentsSQL = "SELECT * FROM APPOINTMENT WHERE client_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectAppointmentsSQL);
            preparedStatement.setLong(1, client.getId());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                Long id = resultSet.getLong("id");
                LocalDateTime date = resultSet.getObject("date", LocalDateTime.class);
                Long doctorId = resultSet.getLong("doctor_id");

                Doctor doctor = findDoctorById(connection, doctorId);

                Appointment appointment = new Appointment(date, doctor, client);
                appointments.add(appointment);
            }

            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving appointments from the database: " + e.getMessage());
        }
        AuditService.logAction("ReadAppointment");
        return appointments;
    }

    public List<Appointment> getAppointmentsForDoctorByName(Connection connection, String doctorName) {
        List<Appointment> appointments = new ArrayList<>();

        if (connection == null) {
            System.out.println("Connection is null. Cannot retrieve appointments.");
            return appointments;
        }

        try {
            Doctor doctor = findDoctorByName(connection, doctorName);

            if (doctor == null) {
                System.out.println("Doctor not found.");
                return appointments;
            }

            String selectAppointmentsSQL = "SELECT * FROM APPOINTMENT WHERE doctor_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectAppointmentsSQL);
            preparedStatement.setLong(1, doctor.getId());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                LocalDateTime date = resultSet.getObject("date", LocalDateTime.class);
                Long clientId = resultSet.getLong("client_id");

                Client client = findClientById(connection, clientId);

                Appointment appointment = new Appointment(date, doctor, client);
                appointments.add(appointment);
            }

            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving appointments from the database: " + e.getMessage());
        }
        AuditService.logAction("ReadAppointment");
        return appointments;
    }



}
