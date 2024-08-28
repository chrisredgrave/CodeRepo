package odi_sdk;
 
import java.util.Collection;
import java.util.Iterator;
 
import oracle.odi.core.OdiInstance;
import oracle.odi.core.config.MasterRepositoryDbInfo;
import oracle.odi.core.config.OdiInstanceConfig;
import oracle.odi.core.config.PoolingAttributes;
import oracle.odi.core.config.WorkRepositoryDbInfo;
import oracle.odi.core.persistence.transaction.ITransactionStatus;
import oracle.odi.core.persistence.transaction.support.DefaultTransactionDefinition;
import oracle.odi.core.security.Authentication;
import oracle.odi.domain.project.OdiFolder;
import oracle.odi.domain.project.OdiInterface;
import oracle.odi.domain.project.OdiPackage;
import oracle.odi.domain.project.OdiVariable;
import oracle.odi.domain.project.StepInterface;
import oracle.odi.domain.project.StepOdiCommand;
import oracle.odi.domain.project.StepVariable;
import oracle.odi.domain.project.StepVariable.DeclareVariable;
import oracle.odi.domain.project.StepVariable.EvaluateVariable;
import oracle.odi.domain.project.StepVariable.RefreshVariable;
import oracle.odi.domain.project.StepVariable.SetVariable;
import oracle.odi.domain.project.finder.IOdiFolderFinder;
import oracle.odi.domain.project.finder.IOdiInterfaceFinder;
import oracle.odi.domain.project.finder.IOdiVariableFinder;
import oracle.odi.domain.xrefs.expression.Expression;
 
public class CreatingPackage {
 
    private static String Project_Code;
    private static String Folder_Name;
    private static String Package_Name;
    private static OdiFolder folder;
    /**
     * @param args
     */
    public static void main(String[] args) {
 
               /****** Please change these Parameters *********/
 
        String Url = "jdbc:oracle:thin:@localhost:1521:orcl";
        String Driver="oracle.jdbc.OracleDriver";
        String Master_User="ODI_MASTER_11G";
        String Master_Pass="ODI_MASTER_11G";
        String WorkRep="WORKREP1";
        String Odi_User="SUPERVISOR";
        String Odi_Pass="SUNOPSIS";
 
        Project_Code="XMT";
        Folder_Name ="FOLDER";
        Package_Name="PKG_TEST";
 
// Connection
MasterRepositoryDbInfo masterInfo = new MasterRepositoryDbInfo(Url, Driver, Master_User,Master_Pass.toCharArray(), new PoolingAttributes());
WorkRepositoryDbInfo workInfo = new WorkRepositoryDbInfo(WorkRep, new PoolingAttributes());
OdiInstance odiInstance=OdiInstance.createInstance(new OdiInstanceConfig(masterInfo,workInfo));
Authentication auth = odiInstance.getSecurityManager().createAuthentication(Odi_User,Odi_Pass.toCharArray());
odiInstance.getSecurityManager().setCurrentThreadAuthentication(auth);
ITransactionStatus trans = odiInstance.getTransactionManager().getTransaction(new DefaultTransactionDefinition());
 
         Collection<OdiFolder> fold = ((IOdiFolderFinder) odiInstance
                 .getTransactionalEntityManager().getFinder(OdiFolder.class)).
                 findByName(Folder_Name,Project_Code);
 
         for (Iterator<OdiFolder> it = fold.iterator(); it.hasNext();) {
             folder = (OdiFolder) it.next();
        }
 
        OdiPackage pkg = new OdiPackage(folder, Package_Name);
        // Here are some of the examples based on variable using the methods defined below
        //Declare Example
        OdiVariable var1=getProjVariable(odiInstance,"TEST_VARIABLE1",Project_Code);
        System.out.println(var1.getName());
        StepVariable step1=getDeclareVariable(pkg, var1, var1.getName());
 
        //Set Example
        OdiVariable var2=getProjVariable(odiInstance,"TEST_VARIABLE1",Project_Code);
        StepVariable step2=getSetVariable(pkg, var2, var2.getName(), "15");
 
        //Refresh Example
        OdiVariable var3=getProjVariable(odiInstance,"TEST_VARIABLE1",Project_Code);
        StepVariable step3=getRefreshVariable(pkg, var3, var3.getName());
 
        //Evaluate Example
        OdiVariable var4=getProjVariable(odiInstance, "TEST_VARIABLE1",Project_Code);
        StepVariable step4=getEvaluateVariable(pkg, var4, var4.getName(), "=", "Y");
 
        // Refresh Variable Failure
        OdiVariable var5=getProjVariable(odiInstance, "TEST_VARIABLE2",Project_Code);
        StepVariable step5=getRefreshVariable(pkg, var5, var5.getName());
 
        // Scenario
        StepOdiCommand scen1=getOdiCmnd(pkg, "SCEN",
                 "OdiStartScen -SCEN_NAME=SCEN -SCEN_VERSION=001");
 
        // Interface
        StepInterface intf = getIntf(odiInstance, pkg, "TEST_INTF", "TEST_INTF");
        
        
        // Linking the steps depending on success or Failure .
        // Categorized for easy understanding and link
        //Success
        pkg.setFirstStep(step1);
        step1.setNextStepAfterSuccess(step2);
        step2.setNextStepAfterSuccess(step3);
        step3.setNextStepAfterSuccess(step4);
        step4.setNextStepAfterSuccess(scen1);
        scen1.setNextStepAfterSuccess(intf);
 
        //Failure
        step1.setNextStepAfterFailure(step5);
        step2.setNextStepAfterFailure(step5);
        step3.setNextStepAfterFailure(step5);
        step4.setNextStepAfterFailure(step5);
        scen1.setNextStepAfterFailure(step5);
        intf.setNextStepAfterFailure(step5);
        // Persisting Package
         odiInstance.getTransactionalEntityManager().persist(pkg);
        /*** Change Ends here **/
 
        // Finally close the Instance
        odiInstance.getTransactionManager().commit(trans);
        odiInstance.close();
 
        System.out.println("Completed ");
 
    }
        /*****************************************************/
        // Please find few methods that can be called any number of times for few types of StepType
        /*****************************************************/
        //Find Global Variable
        public static OdiVariable getGlobalVariable(OdiInstance odiInstance,String GlobalVariable) {
            OdiVariable var=((IOdiVariableFinder) odiInstance.getTransactionalEntityManager().getFinder(
                            OdiVariable.class)).findGlobalByName(GlobalVariable);
 
            return var;
        }
 
        //Find Project Variable
        public static OdiVariable getProjVariable(OdiInstance odiInstance,String ProjectVariable,String ProjectCode) {
            OdiVariable var=((IOdiVariableFinder) odiInstance.getTransactionalEntityManager().getFinder(
                            OdiVariable.class)).findByName(ProjectVariable,ProjectCode);
 
            return var;
        }
 
        // Declare Variable
        public static StepVariable getDeclareVariable(OdiPackage pkg,OdiVariable var,String StepName) {
 
            StepVariable stepDecVar = new StepVariable(pkg,var,StepName);
            DeclareVariable DecVar  = new StepVariable.DeclareVariable(stepDecVar);
            stepDecVar.setAction(DecVar);
 
            return stepDecVar;
 
        }
 
        // Refresh Variable
        public static StepVariable getRefreshVariable(OdiPackage pkg,OdiVariable var,String StepName) {
 
            StepVariable stepRefVar = new StepVariable(pkg,var,StepName);
            RefreshVariable RefVar  = new StepVariable.RefreshVariable(stepRefVar);
            stepRefVar.setAction(RefVar);
 
            return stepRefVar;
        }
 
        // Set Variable
        public static StepVariable getSetVariable(OdiPackage pkg,OdiVariable var,String StepName,String Value) {
 
            StepVariable stepSetVar = new StepVariable(pkg,var,StepName);
            SetVariable SetVar  = new StepVariable.SetVariable(stepSetVar);
            SetVar = new StepVariable.SetVariable(Value);
            stepSetVar.setAction(SetVar);
 
            return stepSetVar;
 
        }
 
        // Evaluate Variable
        public static StepVariable getEvaluateVariable(OdiPackage pkg,OdiVariable var,String StepName,String Operator,String Value) {
 
            StepVariable stepEvaVar = new StepVariable(pkg,var,StepName);
            EvaluateVariable EvaVar  = new StepVariable.EvaluateVariable(stepEvaVar);
            EvaVar = new  StepVariable.EvaluateVariable(Operator,Value);
            stepEvaVar.setAction(EvaVar);
 
            return stepEvaVar;
 
        }
 
        // ODI Command
        public static StepOdiCommand getOdiCmnd (OdiPackage pkg,String StepName,String Command) {
 
            StepOdiCommand odicmnd = new StepOdiCommand(pkg,StepName);
            odicmnd.setCommandExpression(new Expression(Command,null, Expression.SqlGroupType.NONE));
            return odicmnd;
 
        }
 
        // Interface
        public static StepInterface getIntf (OdiInstance odiinstance,OdiPackage pkg,String Interface,String StepName) {
 
            StepInterface stepIntf = null;
             Collection<OdiInterface> intf_find = ((IOdiInterfaceFinder) odiinstance.getTransactionalEntityManager().getFinder(OdiInterface.class)).findByName(Interface, Project_Code, Folder_Name);
             for (Iterator<OdiInterface> iterator = intf_find.iterator(); iterator.hasNext();) {
                 OdiInterface    intf = (OdiInterface) iterator.next();
                 stepIntf = new StepInterface(pkg,intf,StepName);
 
                 }return stepIntf;
        }
 
}