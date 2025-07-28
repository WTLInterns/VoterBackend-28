package com.votersystem.data;

import com.votersystem.entity.Vidhansabha;
import com.votersystem.repository.VidhansabhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class VidhansabhaDataInitializer implements CommandLineRunner {
    
    @Autowired
    private VidhansabhaRepository vidhansabhaRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (vidhansabhaRepository.count() > 0) {
            return; // Data already initialized
        }
        
        initializeVidhansabhaData();
    }
    
    private void initializeVidhansabhaData() {
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
        
        // Buldhana
        vidhansabhaRepository.save(new Vidhansabha(21, "Malkapur", "Buldhana", null));
        vidhansabhaRepository.save(new Vidhansabha(22, "Buldhana", "Buldhana", null));
        vidhansabhaRepository.save(new Vidhansabha(23, "Chikhli", "Buldhana", null));
        vidhansabhaRepository.save(new Vidhansabha(24, "Sindhkhed Raja", "Buldhana", null));
        vidhansabhaRepository.save(new Vidhansabha(25, "Mehkar", "Buldhana", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(26, "Khamgaon", "Buldhana", null));
        vidhansabhaRepository.save(new Vidhansabha(27, "Jalgaon (Jamod)", "Buldhana", null));
        
        // Akola
        vidhansabhaRepository.save(new Vidhansabha(28, "Akot", "Akola", null));
        vidhansabhaRepository.save(new Vidhansabha(29, "Balapur", "Akola", null));
        vidhansabhaRepository.save(new Vidhansabha(30, "Aakola West", "Akola", null));
        vidhansabhaRepository.save(new Vidhansabha(31, "Akola East", "Akola", null));
        vidhansabhaRepository.save(new Vidhansabha(32, "Murtizapur", "Akola", "SC"));
        
        // Washim
        vidhansabhaRepository.save(new Vidhansabha(33, "Risod", "Washim", null));
        vidhansabhaRepository.save(new Vidhansabha(34, "Washim", "Washim", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(35, "Karanja", "Washim", null));
        
        // Amaravati
        vidhansabhaRepository.save(new Vidhansabha(36, "Dhamangaon Railway", "Amaravati", null));
        vidhansabhaRepository.save(new Vidhansabha(37, "Badnera", "Amaravati", null));
        vidhansabhaRepository.save(new Vidhansabha(38, "Amrawati", "Amaravati", null));
        vidhansabhaRepository.save(new Vidhansabha(39, "Teosa", "Amaravati", null));
        vidhansabhaRepository.save(new Vidhansabha(40, "Daryapur", "Amaravati", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(41, "Melghat", "Amaravati", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(42, "Achalpur", "Amaravati", null));
        vidhansabhaRepository.save(new Vidhansabha(43, "Morshi", "Amaravati", null));
        
        // Wardha
        vidhansabhaRepository.save(new Vidhansabha(44, "Arvi", "Wardha", null));
        vidhansabhaRepository.save(new Vidhansabha(45, "Deoli", "Wardha", null));
        vidhansabhaRepository.save(new Vidhansabha(46, "Hinganghat", "Wardha", null));
        vidhansabhaRepository.save(new Vidhansabha(47, "Wardha", "Wardha", null));
        
        // Nagpur
        vidhansabhaRepository.save(new Vidhansabha(48, "Katol", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(49, "Savner", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(50, "Hingna", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(51, "Umred", "Nagpur", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(52, "Nagpur South West", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(53, "Nagpur South", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(54, "Nagpur East", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(55, "Nagpur Central", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(56, "Nagpur West", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(57, "Nagpur North", "Nagpur", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(58, "Kamthi", "Nagpur", null));
        vidhansabhaRepository.save(new Vidhansabha(59, "Ramtek", "Nagpur", null));
        
        // Bhandara
        vidhansabhaRepository.save(new Vidhansabha(60, "Tumsar", "Bhandara", null));
        vidhansabhaRepository.save(new Vidhansabha(61, "Bhandara", "Bhandara", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(62, "Sakoli", "Bhandara", null));

        // Gondiya
        vidhansabhaRepository.save(new Vidhansabha(63, "Arjuni Morgaon", "Gondiya", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(64, "Tirora", "Gondiya", null));
        vidhansabhaRepository.save(new Vidhansabha(65, "Gondia", "Gondiya", null));
        vidhansabhaRepository.save(new Vidhansabha(66, "Amgaon", "Gondiya", "ST"));

        // Gadchiroli
        vidhansabhaRepository.save(new Vidhansabha(67, "Armori", "Gadchiroli", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(68, "Gadchiroli", "Gadchiroli", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(69, "Aheri", "Gadchiroli", "ST"));

        // Chandrapur
        vidhansabhaRepository.save(new Vidhansabha(70, "Rajura", "Chandrapur", null));
        vidhansabhaRepository.save(new Vidhansabha(71, "Chandrapur", "Chandrapur", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(72, "Ballarpur", "Chandrapur", null));
        vidhansabhaRepository.save(new Vidhansabha(73, "Bramhapuri", "Chandrapur", null));
        vidhansabhaRepository.save(new Vidhansabha(74, "Chimur", "Chandrapur", null));
        vidhansabhaRepository.save(new Vidhansabha(75, "Warora", "Chandrapur", null));

        // Add more constituencies... (continuing with remaining districts)
        // Yavatmal
        vidhansabhaRepository.save(new Vidhansabha(76, "Wani", "Yavatmal", null));
        vidhansabhaRepository.save(new Vidhansabha(77, "Ralegaon", "Yavatmal", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(78, "Yavatmal", "Yavatmal", null));
        vidhansabhaRepository.save(new Vidhansabha(79, "Digras", "Yavatmal", null));
        vidhansabhaRepository.save(new Vidhansabha(80, "Arni", "Yavatmal", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(81, "Pusad", "Yavatmal", null));
        vidhansabhaRepository.save(new Vidhansabha(82, "Umarkhed", "Yavatmal", "SC"));

        // Continue with remaining constituencies
        // Nanded
        vidhansabhaRepository.save(new Vidhansabha(83, "Kinwat", "Nanded", null));
        vidhansabhaRepository.save(new Vidhansabha(84, "Hadgaon", "Nanded", null));
        vidhansabhaRepository.save(new Vidhansabha(85, "Bhokar", "Nanded", null));
        vidhansabhaRepository.save(new Vidhansabha(86, "Nanded North", "Nanded", null));
        vidhansabhaRepository.save(new Vidhansabha(87, "Nanded South", "Nanded", null));
        vidhansabhaRepository.save(new Vidhansabha(88, "Loha", "Nanded", null));
        vidhansabhaRepository.save(new Vidhansabha(89, "Naigaon", "Nanded", null));
        vidhansabhaRepository.save(new Vidhansabha(90, "Deglur", "Nanded", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(91, "Mukhed", "Nanded", null));

        // Hingoli
        vidhansabhaRepository.save(new Vidhansabha(92, "Basmath", "Hingoli", null));
        vidhansabhaRepository.save(new Vidhansabha(93, "Kalamnuri", "Hingoli", null));
        vidhansabhaRepository.save(new Vidhansabha(94, "Hingoli", "Hingoli", null));

        // Palghar
        vidhansabhaRepository.save(new Vidhansabha(128, "Dahanu", "Palghar", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(129, "Vekramgrth", "Palghar", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(130, "Palghar", "Palghar", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(131, "Boisar", "Palghar", "ST"));
        vidhansabhaRepository.save(new Vidhansabha(132, "Nalasopara", "Palghar", null));
        vidhansabhaRepository.save(new Vidhansabha(133, "Vasai", "Palghar", null));

        // Pune (key constituencies)
        vidhansabhaRepository.save(new Vidhansabha(195, "Junnar", "Pune", null));
        vidhansabhaRepository.save(new Vidhansabha(201, "Baramati", "Pune", null));
        vidhansabhaRepository.save(new Vidhansabha(205, "Chinchwad", "Pune", null));
        vidhansabhaRepository.save(new Vidhansabha(206, "Pimpri", "Pune", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(209, "Shivajinagar", "Pune", null));
        vidhansabhaRepository.save(new Vidhansabha(210, "Kothrud", "Pune", null));

        // Mumbai (key constituencies)
        vidhansabhaRepository.save(new Vidhansabha(178, "Dharavi", "Mumbai City", "SC"));
        vidhansabhaRepository.save(new Vidhansabha(181, "Mahim", "Mumbai City", null));
        vidhansabhaRepository.save(new Vidhansabha(182, "Worli", "Mumbai City", null));
        vidhansabhaRepository.save(new Vidhansabha(185, "Malabar Hill", "Mumbai City", null));

        // Final constituency
        vidhansabhaRepository.save(new Vidhansabha(288, "Jat", "Sangli", null));
    }
}
