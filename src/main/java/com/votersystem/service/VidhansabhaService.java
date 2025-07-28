package com.votersystem.service;

import com.votersystem.entity.Vidhansabha;
import com.votersystem.repository.VidhansabhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VidhansabhaService {

    @Autowired
    private VidhansabhaRepository vidhansabhaRepository;

    public List<Vidhansabha> getAllConstituencies() {
        return vidhansabhaRepository.findAll();
    }

    public Optional<Vidhansabha> getByVidhansabhaNo(Integer vidhansabhaNo) {
        return vidhansabhaRepository.findByVidhansabhaNo(vidhansabhaNo);
    }

    public List<Vidhansabha> getByDistrict(String districtName) {
        return vidhansabhaRepository.findByDistrictNameIgnoreCase(districtName);
    }

    public List<Vidhansabha> searchByNumberOrName(String searchTerm) {
        return vidhansabhaRepository.searchByNumberOrName(searchTerm);
    }

    public List<String> getAllDistricts() {
        return vidhansabhaRepository.findAllDistricts();
    }

    public List<Vidhansabha> getByCategory(String category) {
        return vidhansabhaRepository.findByCategory(category);
    }

    public void initializeData() {
        // Check if data already exists
        if (vidhansabhaRepository.count() > 0) {
            return; // Data already initialized
        }

        // Initialize with the provided data
        createVidhansabhaData();
    }

    private void createVidhansabhaData() {
        // Nandurabar
        vidhansabhaRepository.save(new Vidhansabha(1, "Akkalkuwa", "Nandurabar", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(2, "Shahada", "Nandurabar", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(3, "Nandurbar", "Nandurabar", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(4, "Nawapur", "Nandurabar", "ST"));

        // Dhule
        vidhansabhaRepository.save(new Vidhansabha(5, "Sakri", "Dhule", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(6, "Dhule Rural", "Dhule", null));
        vidhansabhaRepository.save(new Vidhansabha(7, "Dhule City", "Dhule", null));
        vidhansabhaRepository.save(new Vidhansabha(8, "Sindhkheda", "Dhule", null));
        vidhansabhaRepository.save(new Vidhansabha(9, "Shirpur", "Dhule", "ST"));

        // Jalgaon
        vidhansabhaRepository.save(new Vidhansabha(10, "Chopda", "Jalgaon", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(11, "Raver", "Jalgaon", null));
        vidhansabhaRepository.save(new Vidhansabha(12, "Bhusawal", "Jalgaon", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(13, "Jalgaon City", "Jalgaon", null));
        vidhansabhaRepository.save(new Vidhansabha(14, "Jalgaon Rural", "Jalgaon", null));
        vidhansabhaRepository.save(new Vidhansabha(15, "Amalner", "Jalgaon", null));
        vidhansabhaRepository.save(new Vidhansabha(16, "Erandol", "Jalgaon", null));
        vidhansabhaRepository.save(new Vidhansabha(17, "Chalisgaon", "Jalgaon", null));
        vidhansabhaRepository.save(new Vidhansabha(18, "Pachora", "Jalgaon", null));
        vidhansabhaRepository.save(new Vidhansabha(19, "Jamner", "Jalgaon", null));
        vidhansabhaRepository.save(new Vidhansabha(20, "Muktainagar", "Jalgaon", null));

        // Continue with more data...
        // This is just a sample, we'll add more in the next chunk
    }
}