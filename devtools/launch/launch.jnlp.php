<?php

$applet = $_GET['applet']=="true";
$webstart = !$applet;

if ($webstart){
  header('Content-Type: application/x-java-jnlp-file');
  header("Content-Disposition: attachment; filename=GenomeView.jnlp");
  header('Pragma: no-cache');
}

echo('<?xml version="1.0" encoding="utf-8"?>');

//$siteroot = 'http://' . $_SERVER["HTTP_HOST"] . str_replace(basename($_SERVER["PHP_SELF"]), "", $_SERVER["PHP_SELF"]);
//$siteroot="http://sourceforge.net/projects/genomeview/files/webstart/";
//$siteroot="http://downloads.sourceforge.net/project/genomeview/webstart/";
$siteroot="http://localhost/";


$param = html_entity_decode($_SERVER["QUERY_STRING"]);

$this_file = "launch.jnlp.php";
if (!empty($param)){
  $this_file.="?".$param;
}


//Usage counter
/*
$link=mysql_connect(localhost,"******","*******");
mysql_select_db("web_abeel",$link);

$ip=$_SERVER["REMOTE_ADDR"];
$mparam=mysql_real_escape_string($ip.'/'.$param,$link);
$sql="insert into genomeview_launch set ip=INET_ATON('$ip'), parameters='$mparam'";
mysql_query($sql,$link);
mysql_close($link) ; 
*/

$jars[]="genomeview-922.jar";
$jars[]="jannot_992.jar";
$jars[]="jargs.jar";
$jars[]="commons-logging.jar";
$jars[]="sam-938.jar";
$jars[]="ajt_180.jar";
$jars[]="collections-1.0.jar";

?>

<jnlp 
  <?php if ($webstart):   ?>
  spec="1.0+"
  codebase="<?=$siteroot?>"
  href="<?=$siteroot?><?=$this_file?>"
  <?php endif; ?>
>
<information>
  <title>GenomeView</title>
  <vendor>Thomas Abeel</vendor>
  <homepage href="http://genomeview.sf.net" />
  <icon href="http://broadinstitute.org/software/genomeview/gv2.png" width="47" height="47"  />
 <?php
 /*
  <offline-allowed/>
  <shortcut online="false">
  	<desktop/>
  </shortcut>
  <association mime-type="application-x/genomeview-fasta" extensions="fasta" />    
  <association mime-type="application-x/genomeview-gff" extensions="gff" />
  <association mime-type="application-x/genomeview-fa" extensions="fa" />
  */
  ?>
  <description>GenomeView: Genome Browser and Curator</description>
</information>
<security>
  <all-permissions />
</security>
<resources>
 <j2se version="1.6+" java-vm-args="-ea" initial-heap-size="128M" max-heap-size="1000M"/> 
  	 <?php
  foreach ($jars as $jar_file) {
     echo "<jar href=\"" . $siteroot . "" . $jar_file . "\" />\n";
  }
?>
  
</resources>

<?php
//strip classic url parameters
if (!empty($param)){
  $param_location = strrpos($param, "&")+1;
  $param = substr($param, $param_location);
}
?>

<?php if ($webstart): ?>
<application-desc main-class="net.sf.genomeview.gui.GenomeView">
<?php 
if (!empty($param)){
  $param=str_replace("%25","%",$param);
  $params=explode("%20",$param);
  foreach($params as $single){
    $single=str_replace("%3f","?",$single);
    echo "<argument>". $single."</argument>";
  }
} 
?>
</application-desc>
<?php endif; ?>


<?php if ($applet): ?>
<applet-desc name="GenomeView Demo Applet"
      main-class="net.sf.genomeview.gui.GVApplet"
      width="800"
      height="500">   
  </applet-desc>
<?php endif; ?>

</jnlp>
