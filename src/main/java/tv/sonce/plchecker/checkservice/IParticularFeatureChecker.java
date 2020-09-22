package tv.sonce.plchecker.checkservice;

import tv.sonce.plchecker.PLKeeper;

import java.io.IOException;
import java.util.Map;

// Возвращает <Название проверяемого параметра, Количество найденых ошибок>
public interface IParticularFeatureChecker {
    Map<String, Integer> checkFeature(PLKeeper plKeeper) throws IOException;
}
