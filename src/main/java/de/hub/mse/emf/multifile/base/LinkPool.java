package de.hub.mse.emf.multifile.base;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;

public class LinkPool<L> {

    @Delegate
    private List<L> pool = new ArrayList<>();

    public L getRandomLink(SourceOfRandomness source) {
        int index = source.nextInt(pool.size());
        return pool.get(index);
    }
}
