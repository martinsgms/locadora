package br.ce.wcaquino.matchers;

import java.util.Calendar;

public class MatchersProprios {
    
    public static DiaSemanaMatcher caiem(Integer diaSemana) {
        return new DiaSemanaMatcher(diaSemana);
    }
    
    public static DiaSemanaMatcher caiNumaSegunda() {
        return new DiaSemanaMatcher(Calendar.MONDAY);
    }
    
    public static DiferencaDiasMatcher ehHoje() {
        return new DiferencaDiasMatcher(0);
    }
    
    public static DiferencaDiasMatcher ehDaquiNDias(Integer dias) {
        return new DiferencaDiasMatcher(dias);
    }
    
    public static DiferencaDiasMatcher ehAmanha() {
        return new DiferencaDiasMatcher(1);
    }

}
