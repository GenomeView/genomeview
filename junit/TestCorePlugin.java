/**
 * %HEADER%
 */
package junit;

import net.sf.genomeview.plugin.PluginLoader;

import org.junit.Test;


public class TestCorePlugin {
    @Test
    public void testLoad() {
        PluginLoader.load(null);
    }
}
