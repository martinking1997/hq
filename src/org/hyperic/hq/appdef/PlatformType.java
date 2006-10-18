package org.hyperic.hq.appdef;

import org.hyperic.hq.appdef.shared.PlatformTypeLocal;
import org.hyperic.hq.appdef.shared.PlatformLocal;
import org.hyperic.hq.appdef.shared.PlatformValue;
import org.hyperic.hq.appdef.shared.AgentPK;
import org.hyperic.hq.appdef.shared.AIPlatformValue;
import org.hyperic.hq.appdef.shared.PlatformTypeValue;
import org.hyperic.hq.appdef.shared.PlatformTypePK;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import java.util.Collection;
import java.util.Set;

/**
 * 
 */
public class PlatformType extends AppdefResourceType
{
    private String os;
    private String osVersion;
    private String arch;
    private String plugin;
    private Collection serverTypes;
    private Collection platforms;

    /**
     * default constructor
     */
    public PlatformType()
    {
        super();
    }

    // Property accessors
    public String getOs()
    {
        return this.os;
    }

    public void setOs(String os)
    {
        this.os = os;
    }

    public String getOsVersion()
    {
        return this.osVersion;
    }

    public void setOsVersion(String osVersion)
    {
        this.osVersion = osVersion;
    }

    public String getArch()
    {
        return this.arch;
    }

    public void setArch(String arch)
    {
        this.arch = arch;
    }

    public String getPlugin()
    {
        return this.plugin;
    }

    public void setPlugin(String plugin)
    {
        this.plugin = plugin;
    }

    public Collection getServerTypes()
    {
        return this.serverTypes;
    }

    public void setServerTypes(Collection servers)
    {
        this.serverTypes = servers;
    }

    public Collection getPlatforms()
    {
        return this.platforms;
    }

    public void setPlatforms(Collection platforms)
    {
        this.platforms = platforms;
    }

    public PlatformLocal createPlatform(PlatformValue platform, AgentPK agent)
    throws CreateException
    {
        return null;
    }

    public PlatformLocal createPlatform(AIPlatformValue aiplatform,
                                        String initialOwner)
    throws CreateException
    {
        return null;
    }

    public PlatformTypeValue getPlatformTypeValue()
    {
        return null;
    }

    public PlatformTypeValue getPlatformTypeValueObject()
    {
        return null;
    }

    public Set getServerTypeSnapshot()
    {
        return null;
    }

    private PlatformTypePK pkey = new PlatformTypePK();
    /**
     * @deprecated use getId()
     * @return
     */
    public PlatformTypePK getPrimaryKey()
    {
        pkey.setId(getId());
        return pkey;
    }
}
