package ru.Darvin.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.Darvin.DTO.Mapper.SuppliesMapperImpl;
import ru.Darvin.DTO.SuppliesDTO;
import ru.Darvin.DTO.SupplyDTO;
import ru.Darvin.Entity.StockSupplies;
import ru.Darvin.Exception.EquipmentNotFoundException;
import ru.Darvin.Repository.StockSuppliesRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StockSuppliesService {

    @Autowired
    private SuppliesService suppliesService;

    @Autowired
    private StockSuppliesRepository suppliesRepository;


    //создание нового расходного материала в складе
    public StockSupplies createNewStockSupplies (SupplyDTO suppliesDTO){
        //ищем в бд
        Optional<StockSupplies> existingStock = suppliesRepository.findByNomenclatureCode(suppliesDTO.getNomenclatureCode());
        //если нашли, то задаем количество и сохраняем
        if (existingStock.isPresent()){
            StockSupplies stockSupplies = existingStock.get();
            stockSupplies.setQuantity(suppliesDTO.getQuantity());
            stockSupplies.setNomenclature(suppliesDTO.getNomenclature());
            return suppliesRepository.save(stockSupplies);
        }
        //если не нашли, то создаем новую сущность
        SuppliesDTO findSuppliesDTO;

        //ищем в базе 1с, если нашли то задаем количество и сохраняем
        try {
            findSuppliesDTO = suppliesService.getSupplies(suppliesDTO.getNomenclatureCode());
        }catch (EquipmentNotFoundException e){
            SuppliesDTO data = new SuppliesDTO();
            data.setQuantity(suppliesDTO.getQuantity());
            data.setNomenclature(suppliesDTO.getNomenclature());
            findSuppliesDTO = data;
            findSuppliesDTO.setNomenclatureCode(generateUniqueCode());
        }
        //если не нашли, то создаем новую
        StockSupplies stockSupplies = new StockSupplies();

        stockSupplies.setNomenclatureCode(findSuppliesDTO.getNomenclatureCode());
        stockSupplies.setMol(findSuppliesDTO.getMol());
        stockSupplies.setQuantity(suppliesDTO.getQuantity());
        stockSupplies.setNomenclature(findSuppliesDTO.getNomenclature());

        return suppliesRepository.save(stockSupplies);
    }

    public void deleteStockSupplies (String nomenclatureCode){
       StockSupplies stockSupplies = suppliesRepository.findByNomenclatureCode(nomenclatureCode)
               .orElseThrow(()-> new RuntimeException("Расходный материал с кодом " + nomenclatureCode + " не найден"));
        suppliesRepository.delete(stockSupplies);
    }

    public List<StockSupplies> getStockSupplies () {
        return suppliesRepository.findAll();
    }

    private String generateUniqueCode() {
        String generatedCode;

            // Генерируем код
            long currentTimeMillis = System.currentTimeMillis();
            String randomPart = String.format("%04d", (int)(Math.random() * 10000));
            generatedCode = String.format("00-%012d-%s", currentTimeMillis % 1_000_000_000_000L, randomPart);

        return generatedCode;
    }


    public void updateStockQuantity(String nomenclatureCode, int quantityChange) {
        StockSupplies stockSupply = suppliesRepository.findByNomenclatureCode(nomenclatureCode)
                .orElseThrow(() -> new EquipmentNotFoundException("Материалы с номенклатурным кодом " + nomenclatureCode + " не найдены на складе"));

        int newQuantity = stockSupply.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Недостаточно материалов на складе: " + stockSupply.getNomenclature());
        }

        stockSupply.setQuantity(newQuantity);
        suppliesRepository.save(stockSupply);
    }

    public StockSupplies getSupplies (String nomenclatureCode){
       return suppliesRepository.findByNomenclatureCode(nomenclatureCode)
                .orElseThrow(() -> new EquipmentNotFoundException("Материалы с номенклатурным кодом " + nomenclatureCode + " не найдены на складе"));
    }

    public SuppliesDTO getSuppliesOrNull(String nomenclatureCode) {
        try {
            return SuppliesMapperImpl.INSTANCE.mapToSuppliesDTO(getSupplies(nomenclatureCode)) ;
        } catch (EquipmentNotFoundException e) {
            return null;
        }
    }
}
