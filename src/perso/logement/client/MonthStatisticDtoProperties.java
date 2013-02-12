package perso.logement.client;

import perso.logement.client.dto.MonthStatisticDto;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface MonthStatisticDtoProperties extends PropertyAccess<MonthStatisticDto> {

  ModelKeyProvider<MonthStatisticDto> key();

  ValueProvider<MonthStatisticDto, String> period();

  ValueProvider<MonthStatisticDto, Integer> month();

  ValueProvider<MonthStatisticDto, Integer> year();

  ValueProvider<MonthStatisticDto, Integer> nbAnnonces();

  ValueProvider<MonthStatisticDto, Integer> nbAnnoncesWithNullPrice();

  ValueProvider<MonthStatisticDto, Integer> priceByMeterSquare();

  ValueProvider<MonthStatisticDto, Short> arrondissement();

  ValueProvider<MonthStatisticDto, String> quartier();
}
