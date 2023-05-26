package de.hub.mse.emf.multifile.impl.opendocument;

import de.hub.mse.emf.multifile.LinkPool;
import de.hub.mse.emf.multifile.PoolBasedGeneratorConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class XlsxGeneratorConfig implements PoolBasedGeneratorConfig<XlsxSheetLink> {

    public String workingDirectory;
    private final int modelHeight;
    private final int modelWidth;

    private final int sheetsPerDocument;

    private final int targetDocumentDepth;

    private final LinkPool<XlsxSheetLink> linkPool = new LinkPool<>();
}
