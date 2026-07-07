package com.verona.cafe.service;

import com.verona.cafe.model.CafeTable;
import com.verona.cafe.model.TableStatus;
import com.verona.cafe.repository.CafeTableRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TableService {
    private final CafeTableRepository tableRepository;

    public TableService(CafeTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    public List<CafeTable> getAllTables() {
        return tableRepository.findAll();
    }

    public CafeTable getTableById(Long id) {
        return tableRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Table ID: " + id));
    }

    public void updateTableStatus(Long id, TableStatus status) {
        CafeTable table = getTableById(id);
        table.setStatus(status);
        tableRepository.save(table);
    }
    
    public CafeTable saveTable(CafeTable table) {
        if (table.getStatus() == null) {
            table.setStatus(TableStatus.AVAILABLE);
        }
        return tableRepository.save(table);
    }
    
    public void deleteTable(Long id) {
        tableRepository.deleteById(id);
    }
}
