import oracle.odi.core.OdiInstance
import oracle.odi.core.persistence.transaction.support.DefaultTransactionDefinition
import java.util.Collection;
import oracle.odi.domain.mapping.*;
import oracle.odi.domain.mapping.finder.IMappingFinder;
import oracle.odi.core.persistence.transaction.ITransactionDefinition;
import oracle.odi.core.persistence.transaction.support.DefaultTransactionDefinition;
import oracle.odi.core.persistence.transaction.ITransactionManager;
import oracle.odi.core.persistence.transaction.ITransactionStatus
import oracle.odi.core.persistence.transaction.ITransactionStatus
import oracle.odi.core.persistence.transaction.support.ITransactionCallback
import oracle.odi.core.persistence.transaction.support.TransactionTemplate;
import oracle.odi.domain.project.OdiFolder
import oracle.odi.domain.project.finder.IOdiFolderFinder
import oracle.odi.generation.support.OdiScenarioGeneratorImpl
import oracle.odi.runtime.agent.invocation.RemoteRuntimeAgentInvoker
import oracle.odi.runtime.agent.invocation.StartupParams

def folderName = '<FOLDER_NAME>'
def projectCode = '<PROJECT_CODE>'

def tm = odiInstance.getTransactionManager()
def tem = odiInstance.getTransactionalEntityManager()
def txn = odiInstance.getTransactionManager().getTransaction(new DefaultTransactionDefinition())

agent = new RemoteRuntimeAgentInvoker("http://<HOSTNAME>:<PORT>/oraclediagent", "SUPERVISOR", "<password>".toCharArray())
startupParams = new StartupParams()

tem.flush()
IMappingFinder mappingFinder = (IMappingFinder)odiInstance.getTransactionalEntityManager().getFinder(Mapping.class);
IOdiFolderFinder folderFinder = (IOdiFolderFinder)odiInstance.getTransactionalEntityManager().getFinder(OdiFolder.class);
OdiFolder folder = null;
Collection<OdiFolder> folders = folderFinder.findByName(folderName, projectCode);
if (folders != null && folders.size() > 0)
   folder = folders.iterator().next();

Collection<Mapping> mappings = mappingFinder.findByProject(projectCode, folderName);

for (Mapping map : mappings )
{

    try
    {

    println ("Processing Mapping:" + map.getName());

    /* ----
        if already scenario is generated remove it manually before you generate it again

    ---- */

    def scen=new oracle.odi.generation.support.OdiScenarioGeneratorImpl(odiInstance)
    scen.generateScenario(map,map.getName().toUpperCase(),"001")

    }
    catch (Exception e)
    {
        println ("Error occured for Mapping:" + e.getMessage());
        e.printStackTrace();
    }

}

tm.commit( txn )