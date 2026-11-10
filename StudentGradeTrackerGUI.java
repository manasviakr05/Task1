import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class StudentGradeTrackerGUI extends JFrame {

    // Simple model class for one student
    static class Student {
        private String name;
        private double grade;

        Student(String name, double grade) {
            this.name = name;
            this.grade = grade;
        }

        String getName() { return name; }
        double getGrade() { return grade; }
        void setName(String name) { this.name = name; }
        void setGrade(double grade) { this.grade = grade; }

        String getLetter() {
            if (grade >= 90) return "A";
            if (grade >= 80) return "B";
            if (grade >= 70) return "C";
            if (grade >= 60) return "D";
            return "F";
        }
    }

    // Data store
    private final ArrayList<Student> students = new ArrayList<>();

    // UI components
    private final JTextField nameField = new JTextField(15);
    private final JTextField gradeField = new JTextField(6);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"No.", "Name", "Grade", "Letter"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel totalLabel = new JLabel();
    private final JLabel avgLabel = new JLabel();
    private final JLabel highLabel = new JLabel();
    private final JLabel lowLabel = new JLabel();

    public StudentGradeTrackerGUI() {
        super("Student Grade Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildStatsPanel(), BorderLayout.SOUTH);

        refresh();
        setSize(650, 480);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
    }

    // ---------- UI building ----------
    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Student details"));

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update Selected");
        JButton removeBtn = new JButton("Remove Selected");

        addBtn.addActionListener(e -> addStudent());
        updateBtn.addActionListener(e -> updateStudent());
        removeBtn.addActionListener(e -> removeStudent());
        // Pressing Enter in the grade box adds the student
        gradeField.addActionListener(e -> addStudent());

        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Grade (0-100):"));
        panel.add(gradeField);
        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(removeBtn);
        return panel;
    }

    private JScrollPane buildTablePanel() {
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(3).setMaxWidth(80);

        // Copy the selected row into the input fields
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    Student s = students.get(row);
                    nameField.setText(s.getName());
                    gradeField.setText(String.valueOf(s.getGrade()));
                }
            }
        });
        return new JScrollPane(table);
    }

    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        JPanel labels = new JPanel(new GridLayout(2, 2, 10, 4));
        labels.add(totalLabel);
        labels.add(avgLabel);
        labels.add(highLabel);
        labels.add(lowLabel);

        JButton reportBtn = new JButton("Show Summary Report");
        reportBtn.addActionListener(e -> showSummaryReport());

        panel.add(labels, BorderLayout.CENTER);
        panel.add(reportBtn, BorderLayout.EAST);
        return panel;
    }

    // ---------- actions ----------
    private void addStudent() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter a student name.");
            return;
        }
        if (findByName(name, null) != null) {
            showError("A student with that name already exists.");
            return;
        }
        Double grade = parseGrade();
        if (grade == null) return;

        students.add(new Student(name, grade));
        clearFields();
        refresh();
    }

    private void updateStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a student in the table first.");
            return;
        }
        Student selected = students.get(row);
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter a student name.");
            return;
        }
        if (findByName(name, selected) != null) {
            showError("Another student already has that name.");
            return;
        }
        Double grade = parseGrade();
        if (grade == null) return;

        selected.setName(name);
        selected.setGrade(grade);
        clearFields();
        refresh();
    }

    private void removeStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a student in the table first.");
            return;
        }
        Student s = students.get(row);
        int answer = JOptionPane.showConfirmDialog(this,
                "Remove " + s.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            students.remove(row);
            clearFields();
            refresh();
        }
    }

    private void showSummaryReport() {
        if (students.isEmpty()) {
            showError("No students to report yet.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-4s %-20s %-8s %-6s%n", "No.", "Name", "Grade", "Letter"));
        sb.append("------------------------------------------\n");
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            sb.append(String.format("%-4d %-20s %-8.2f %-6s%n",
                    i + 1, s.getName(), s.getGrade(), s.getLetter()));
        }
        sb.append("------------------------------------------\n");
        sb.append("Total students: ").append(students.size()).append("\n");
        sb.append(String.format("Average: %.2f%n", average()));
        sb.append(String.format("Highest: %.2f (%s)%n", highest().getGrade(), highest().getName()));
        sb.append(String.format("Lowest : %.2f (%s)%n", lowest().getGrade(), lowest().getName()));

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(460, 300));
        JOptionPane.showMessageDialog(this, scroll, "Summary Report", JOptionPane.PLAIN_MESSAGE);
    }

    // ---------- calculations ----------
    private double average() {
        double sum = 0;
        for (Student s : students) sum += s.getGrade();
        return sum / students.size();
    }

    private Student highest() {
        Student best = students.get(0);
        for (Student s : students) if (s.getGrade() > best.getGrade()) best = s;
        return best;
    }

    private Student lowest() {
        Student worst = students.get(0);
        for (Student s : students) if (s.getGrade() < worst.getGrade()) worst = s;
        return worst;
    }

    // ---------- helpers ----------
    private void refresh() {
        tableModel.setRowCount(0);
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            tableModel.addRow(new Object[]{
                    i + 1, s.getName(), String.format("%.2f", s.getGrade()), s.getLetter()});
        }
        totalLabel.setText("Total students: " + students.size());
        if (students.isEmpty()) {
            avgLabel.setText("Average: -");
            highLabel.setText("Highest: -");
            lowLabel.setText("Lowest: -");
        } else {
            avgLabel.setText(String.format("Average: %.2f", average()));
            highLabel.setText(String.format("Highest: %.2f (%s)",
                    highest().getGrade(), highest().getName()));
            lowLabel.setText(String.format("Lowest: %.2f (%s)",
                    lowest().getGrade(), lowest().getName()));
        }
    }

    private Student findByName(String name, Student ignore) {
        for (Student s : students) {
            if (s != ignore && s.getName().equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    private Double parseGrade() {
        try {
            double g = Double.parseDouble(gradeField.getText().trim());
            if (g < 0 || g > 100) {
                showError("Grade must be between 0 and 100.");
                return null;
            }
            return g;
        } catch (NumberFormatException ex) {
            showError("Please enter a valid numeric grade.");
            return null;
        }
    }

    private void clearFields() {
        nameField.setText("");
        gradeField.setText("");
        table.clearSelection();
        nameField.requestFocusInWindow();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentGradeTrackerGUI().setVisible(true));
    }
}
