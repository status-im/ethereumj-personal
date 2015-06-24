package org.ethereum.android_app;

import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.ethereum.android.datasource.LevelDbDataSource;
import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.KeyValueDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.util.encoders.Hex;

import java.io.File;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.is;

@RunWith(AndroidJUnit4.class)
public class LevelDbDataSourceTest extends ActivityInstrumentationTestCase2<TestActivity> {

    private TestActivity activity;

    public LevelDbDataSourceTest() {

        super(TestActivity.class);
    }

    @Before
    public void setUp() throws Exception {

        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = getActivity();
        System.setProperty("sun.arch.data.model", "32");
        System.setProperty("leveldb.mmap", "false");
        String databaseFolder = null;
        File extStore = Environment.getExternalStorageDirectory();
        if (extStore.exists()) {
            databaseFolder = extStore.getAbsolutePath();
        } else {
            databaseFolder = activity.getApplicationInfo().dataDir;
        }
        if (databaseFolder != null) {
            System.out.println("Database folder: " + databaseFolder);
            SystemProperties.CONFIG.setDataBaseDir(databaseFolder);
        }
    }

    @Test
    public void testSet0() {

        assertThat(activity, notNullValue());
        assertThat(getInstrumentation(), notNullValue());
    }

    @Test
    public void testSet1() {

        KeyValueDataSource dataSource = createDataSource("test-state");
        try {
            byte[] key = Hex.decode("a1a2a3");
            byte[] val = Hex.decode("b1b2b3");

            dataSource.put(key, val);
            byte[] val2 = dataSource.get(key);

            assertThat(Hex.toHexString(val), is(Hex.toHexString(val2)));
        } finally {
            clear(dataSource);
        }
    }

    @Test
    public void testSet2() {

        KeyValueDataSource states = createDataSource("test-state");
        KeyValueDataSource details = createDataSource("test-details");

        try {
            byte[] key = Hex.decode("a1a2a3");
            byte[] val1 = Hex.decode("b1b2b3");
            byte[] val2 = Hex.decode("c1c2c3");

            states.put(key, val1);
            details.put(key, val2);

            byte[] res1 = states.get(key);
            byte[] res2 = details.get(key);

            assertThat(Hex.toHexString(val1), is(Hex.toHexString(res1)));
            assertThat(Hex.toHexString(val2), is(Hex.toHexString(res2)));
        } finally {
            clear(states);
            clear(details);
        }
    }

    @After
    public void tearDown() throws Exception {

        super.tearDown();
    }


    private KeyValueDataSource createDataSource(String name) {

        LevelDbDataSource result = new LevelDbDataSource();
        result.setName(name);
        result.init();
        return result;
    }

    private void clear(KeyValueDataSource dataSource) {

        ((LevelDbDataSource) dataSource).close();
    }

}
