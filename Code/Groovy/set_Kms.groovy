import oracle.odi.core.OdiInstance
import oracle.odi.core.config.OdiInstanceConfig
import oracle.odi.core.config.MasterRepositoryDbInfo
import oracle.odi.core.config.WorkRepositoryDbInfo
import oracle.odi.core.security.Authentication 
import oracle.odi.core.config.PoolingAttributes
import oracle.odi.domain.project.finder.IOdiFolderFinder
import oracle.odi.domain.project.finder.IOdiInterfaceFinder
import oracle.odi.domain.project.OdiFolder
import oracle.odi.domain.project.OdiInterface
import java.util.Collection
import java.io.*
 
import oracle.odi.domain.mapping.finder.IMappingFinder
import oracle.odi.domain.project.finder.IOdiIKMFinder
 
import oracle.odi.core.persistence.transaction.ITransactionDefinition;
import oracle.odi.core.persistence.transaction.support.DefaultTransactionDefinition;
import oracle.odi.core.persistence.transaction.ITransactionManager;
import oracle.odi.core.persistence.transaction.ITransactionStatus;
 
def url = "jdbc:oracle:thin:@EC2AMAZ-SSSUNUR:1521/orcl"
def driver = "oracle.jdbc.OracleDriver"
def schema = "DEV_ODI_REPO"
def schemapwd = "welcome1"
def workrep = "WORKREP"
def odiuser= "SUPERVISOR"
def odiuserpwd = "welcome1"
 
 
def project = "GASSCO_ETL" /*Project Code*/
def folder = "IDC" /*Folder Name*/
def ikmName = 'IKM SQL Control Append (Exadata)'
 
def masterInfo = new MasterRepositoryDbInfo(url, driver, schema, schemapwd.toCharArray(), new PoolingAttributes())
def workInfo = new WorkRepositoryDbInfo(workrep, new PoolingAttributes())
def odiInstance = OdiInstance.createInstance(new OdiInstanceConfig(masterInfo, workInfo))
 
def auth = odiInstance.getSecurityManager().createAuthentication(odiuser, odiuserpwd.toCharArray())
odiInstance.getSecurityManager().setCurrentThreadAuthentication(auth)
 
// Transaction Instance
 
ITransactionDefinition txnDef = new DefaultTransactionDefinition();
ITransactionManager tm = odiInstance.getTransactionManager();
ITransactionStatus txnStatus = tm.getTransaction(txnDef);
 
//--------------------------------------
// Retrieve the folder we are looking for
def odiFolders = ((IOdiFolderFinder)odiInstance.getTransactionalEntityManager().getFinder(OdiFolder.class)).findByName(folder,project)
 
if (odiFolders.size() == 0){
    System.err.println("Error: cannot find folder "+folder+" in project "+project);
    return
}
def odiFolder = (OdiFolder) (odiFolders.toArray()[0])
 
//--------------------------------------
// Retrieve all interfaces in that folder
def OdiMappingList = ((IMappingFinder)odiInstance.getTransactionalEntityManager().getFinder(Mapping.class)).findByProject(project, folder)
 
for (map in OdiMappingList){
        println("**********\nINTERFFACE: " + map.getName())
   
        physicalDesign  = map.getPhysicalDesign(0)
        println("**********\nphysicalDesign: " + physicalDesign.getName())
        nodes = physicalDesign.findNodes(map.getName())
        node  = nodes[0]
        println("**********\nnode: " + node.getName())
         
        foundKMs = ((IOdiIKMFinder) odiInstance.getTransactionalEntityManager().getFinder(OdiIKM.class)).findByName(ikmName,project);
        def km = foundKMs[0]
         
        //node.setIKM(km)
 
        // Set KM Options
        ikmOptions = node.getIKMOptionValues()
        ikmOptions.each {ikmOption ->
          switch (ikmOption.getName())
          {
            //case 'USE_CREATE_TABLE_FOR_APPEND': ikmOption.setValue('True')
            case 'FLOW_CONTROL': ikmOption.setValue(1)
            case 'DELETE_TEMPORARY_OBJECTS': ikmOption.setValue(0)
            case 'NEXT_EXTENT_SIZE' : ikmOption.setValue('1M')
            case 'INITIAL_EXTENT_SIZE' : ikmOption.setValue('64K')
          }
        }
}
 
odiInstance.getTransactionManager().commit(txnStatus);
odiInstance.close();