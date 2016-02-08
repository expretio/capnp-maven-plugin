package org.expretio.maven.plugins.capnp.util;

import static com.google.common.base.MoreObjects.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.expretio.maven.plugins.capnp.util.JavaPlatform.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.expretio.maven.plugins.capnp.util.JavaPlatform.Os;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class NativesManager
{
    public static final String CAPNP_NATIVES_DESCRIPTOR_FILE_NAME =
            "capnp-natives.xml";

    protected static final String CAPNP_NATIVES_DESCRIPTOR_RESOURCE_PATH =
            "META-INF/" + CAPNP_NATIVES_DESCRIPTOR_FILE_NAME;

    public class NativesInfo
    {
        private String osName;
        private String archName;
        private String capnpPath;
        private URL capnpUrl;
        private String capnpcJavaPath;
        private URL capnpcJavaUrl;
        private String capnpJavaSchemaPath;
        private URL capnpJavaSchemaUrl;

        public String getOsName()
        {
            return osName;
        }

        public String getArchName()
        {
            return archName;
        }

        public String getCapnpPath()
        {
            return capnpPath;
        }

        public URL getCapnpUrl()
        {
            return capnpUrl;
        }

        public String getCapnpcJavaPath()
        {
            return capnpcJavaPath;
        }

        public URL getCapnpcJavaUrl()
        {
            return capnpcJavaUrl;
        }

        public String getCapnpJavaSchemaPath()
        {
            return capnpJavaSchemaPath;
        }

        public URL getCapnpJavaSchemaUrl()
        {
            return capnpJavaSchemaUrl;
        }

        @Override
        public String toString()
        {
            return
                toStringHelper( this )
                    .add( "osName", osName )
                    .add( "archName", archName )
                    .add( "capnpPath", capnpPath )
                    .add( "capnpUrl", capnpUrl )
                    .add( "capnpcJavaPath", capnpcJavaPath )
                    .add( "capnpcJavaUrl", capnpcJavaUrl )
                    .add( "capnpJavaSchemaPath", capnpJavaSchemaPath )
                    .add( "capnpJavaSchemaUrl", capnpJavaSchemaUrl )
                    .toString();
        }
    }

    private final List<URL> resourceUrls = new ArrayList<>();
    private final Table<String, String, NativesInfo> nativesTable = HashBasedTable.create();

    public NativesManager() {}

    public void addResourceUrl( URL url )
    {
        resourceUrls.add( url );
    }

    public void registerAllDescriptors()
        throws NativesManagerException
    {
        try
        {
            ClassLoader cl = new URLClassLoader( resourceUrls.toArray( new URL[ resourceUrls.size() ] ) );

            for ( URL url : findAllDescriptors( cl ) )
            {
                registerFromDescriptor( url, cl );
            }
        }
        catch ( Exception e )
        {
            throw new NativesManagerException( e );
        }
    }

    public void registerFromDescriptor( URL url )
        throws NativesManagerException
    {
        registerFromDescriptor( url, Thread.currentThread().getContextClassLoader() );
    }

    public void registerFromDescriptor( URL url, ClassLoader cl )
        throws NativesManagerException
    {
        try
        {
            XMLConfiguration config = new XMLConfiguration();

            try ( InputStream reader = new BufferedInputStream( url.openStream() ) )
            {
                config.load( reader );
            }

            String basePath = config.getString( "base-path" );

            if ( basePath == null )
            {
                basePath = EMPTY;
            }
            else if ( !basePath.isEmpty() )
            {
                basePath = ( stripEnd( basePath, "/" ) + "/" );
            }

            for ( HierarchicalConfiguration nativesConfig : config.configurationsAt( "natives" ) )
            {
                String osName = nativesConfig.getString( "os[@name]" );

                for ( HierarchicalConfiguration archConfig : nativesConfig.configurationsAt( "arch" ) )
                {
                    NativesInfo natives = new NativesInfo();

                    natives.osName = osName.toUpperCase();
                    natives.archName = getCanonicalArchitecture( archConfig.getString( "[@name]" ) ).toUpperCase();
                    natives.capnpPath = ( basePath + archConfig.getString( "capnp-exec-path" ) );
                    natives.capnpcJavaPath = ( basePath + archConfig.getString( "capnpc-java-exec-path" ) );
                    natives.capnpJavaSchemaPath = ( basePath + archConfig.getString( "capnp-java-schema-path" ) );

                    natives.capnpUrl = cl.getResource( natives.capnpPath );
                    natives.capnpcJavaUrl = cl.getResource( natives.capnpcJavaPath );
                    natives.capnpJavaSchemaUrl = cl.getResource( natives.capnpJavaSchemaPath );

                    nativesTable.put( natives.osName, natives.archName, natives );
                }
            }
        }
        catch ( Exception e )
        {
            throw new NativesManagerException( e );
        }
    }

    public NativesInfo getNativesInfo( String osName, String archName )
    {
        return nativesTable.get( osName, archName );
    }

    public NativesInfo getNativesInfoForCurrentPlatform()
    {
        Os currentOs = Os.getCurrentOs();
        String currentArch = getCanonicalArchitecture( StandardSystemProperty.OS_ARCH.value() ).toUpperCase();

        return nativesTable.get( currentOs.name(), currentArch );
    }

    protected List<URL> findAllDescriptors( ClassLoader cl )
        throws IOException
    {
        return Collections.list( cl.getResources( CAPNP_NATIVES_DESCRIPTOR_RESOURCE_PATH ) );
    }
}
