import path from 'node:path';
import { pathToFileURL } from 'node:url';
process.env.RUNTIME_NODE_MODULES='C:/Users/yaram/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules';
const SKILL_DIR='C:/Users/yaram/.codex/plugins/cache/openai-primary-runtime/presentations/26.1007.11041/skills/presentations';
const workspaceDir='C:/Users/yaram/Documents/workspace-spring-tools-for-eclipse-4.31.0.RELEASE/complaint';
const FINAL_PPTX=path.join(workspaceDir,'output','College_Grievance_Management_System_WebGenZ.pptx');
const { finalizePresentation }=await import(pathToFileURL(path.join(SKILL_DIR,'container_tools/artifact_tool_utils.mjs')).href);
const result=await finalizePresentation({
  explicitTotalSlideCount:17,
  workspaceDir,
  candidatePath:path.join(workspaceDir,'.deck_build','candidate.pptx'),
  finalPath:FINAL_PPTX,
  pythonExecutable:'C:/Users/yaram/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe',
  integrityValidatorPath:path.join(SKILL_DIR,'container_tools/inspect_presentation_package_integrity.py'),
  layoutValidatorPath:path.join(SKILL_DIR,'container_tools/inspect_presentation_layout_geometry.py'),
  layoutArgs:['--expected-slide-size-emu','12192000,6858000','--validate-bullet-geometry','--validate-heading-fit'],
  requiredNativeTableOwnerSlides:[],
  requiredNativeChartOwnerSlides:[],
  fontPolicy:{basis:'reference',families:['Times New Roman','Arial','Verdana'],referencePath:'C:/Users/yaram/Downloads/WebGenZ- PPT template.pptx',referenceSha256:'824d20b42736a55c8598435aaf8e1d3496e7d447b976f6b510fbd70cf6908ecb'},
  verifyArtifactToolImport:true,
  receiptPath:path.join(workspaceDir,'.deck_build','College_Grievance_Management_System_WebGenZ.validation.json')
});
console.log(JSON.stringify(result,null,2));
