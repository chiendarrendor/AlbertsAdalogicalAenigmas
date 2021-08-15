import java.util.HashMap;
import java.util.Map;

public class FutonUtilities {
    public static Map<Integer,FutonPair> futonPairMapDeepCopy(Map<Integer,FutonPair> orig) {
        Map<Integer,FutonPair> result = new HashMap<>();
        for (Map.Entry<Integer,FutonPair> entry : orig.entrySet()) {
            result.put(entry.getKey(),entry.getValue());
        }
        return result;
    }
}
