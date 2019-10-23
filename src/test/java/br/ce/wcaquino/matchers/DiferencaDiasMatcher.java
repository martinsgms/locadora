package br.ce.wcaquino.matchers;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.ce.wcaquino.utils.DataUtils;

public class DiferencaDiasMatcher extends TypeSafeMatcher<Date> {

    private Integer diferenca;
    
    
    public DiferencaDiasMatcher(Integer diferenca) {
        this.diferenca = diferenca;
    }

    public void describeTo(Description description) {
        Calendar dt = Calendar.getInstance();
        dt.set(Calendar.DAY_OF_WEEK, diferenca);
        String dataFormatada = dt.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, new Locale("en", "US"));
        description.appendText(dataFormatada);
    }

    @Override
    protected boolean matchesSafely(Date data) {
        // TODO Auto-generated method stub
        return DataUtils.isMesmaData(DataUtils.obterDataComDiferencaDias(diferenca), data);
    }

}
