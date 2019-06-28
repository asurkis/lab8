package client;

import collection.CollectionElement;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.time.LocalDateTime;
import java.util.*;

public class LabTableModel implements TableModel {
    private List<CollectionElement> elements = new ArrayList<>();
    private List<TableModelListener> listeners = new LinkedList<>();

    private String[] columnNames = new String[]{"1", "1", "1", "1", "1"};
    private Class[] columnClasses = new Class[]{String.class, Double.class, Double.class, Double.class, LocalDateTime.class};

    @Override
    public int getRowCount() {
        return elements.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CollectionElement element = elements.get(rowIndex);
        switch (columnIndex) {
            case 0: return element.getName();
            case 1: return element.getSize();
            case 2: return element.getPosition().getX();
            case 3: return element.getPosition().getY();
            case 4: return element.getCreationDate();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//        for (TableModelListener l: listeners) {
//            l.tableChanged(new TableModelEvent(this, rowIndex));
//        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    public List<CollectionElement> getElements() {
        return elements;
    }

    public void setElements(List<CollectionElement> elements) {
        this.elements = elements;
        for (TableModelListener l : listeners) {
            l.tableChanged(new TableModelEvent(this));
        }
    }

    public void updateLocale(ResourceBundle bundle) {
        columnNames = new String[]{
                bundle.getString("table.model.name"),
                bundle.getString("table.model.size"),
                bundle.getString("table.model.posX"),
                bundle.getString("table.model.posY"),
                bundle.getString("table.model.creationDate")
        };

        for (TableModelListener l : listeners) {
            l.tableChanged(new TableModelEvent(this));
        }
    }
}
