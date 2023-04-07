package de.hub.mse.emf.multifile;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;

public class LinkPool<L> implements List<L>{

    @Delegate
    private final List<L> pool = new ArrayList<>();

    public L getRandomLink(SourceOfRandomness source) {
        return source.choose(this);
    }
}
