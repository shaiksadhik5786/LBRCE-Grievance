import fs from 'node:fs/promises';
import { pathToFileURL } from 'node:url';
import { createHash } from 'node:crypto';
const moduleUrl=pathToFileURL('C:/Users/yaram/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/@oai/artifact-tool/dist/artifact_tool.mjs').href;
const { FileBlob, PresentationFile } = await import(moduleUrl);
const sourcePath='C:/Users/yaram/Downloads/WebGenZ- PPT template.pptx';
const buildDir='C:/Users/yaram/Documents/workspace-spring-tools-for-eclipse-4.31.0.RELEASE/complaint/.deck_build';
const outputPath='C:/Users/yaram/Documents/workspace-spring-tools-for-eclipse-4.31.0.RELEASE/complaint/output/College_Grievance_Management_System_WebGenZ.pptx';
const BLUE='#006FC0', PALE='#F3F8FC', INK='#111111', GREY='#52616B', WHITE='#FFFFFF';
const p=await PresentationFile.importPptx(await FileBlob.load(sourcePath));
if(p.slides.items.length!==8) throw new Error(`Expected 8 template slides, got ${p.slides.items.length}`);
const titleSlide=p.slides.items[0], contentBase=p.slides.items[1], thankYouSlide=p.slides.items[6];
p.slides.keep([0,1,6]);
const contentSlides=[contentBase];
for(let i=0;i<14;i++) contentSlides.push(contentBase.duplicate());
const slideRecords=await p.inspect({select:'$.slides[*]',include:['id','index']});
const canonicalByIndex=new Map(slideRecords.ndjson.trim().split('\n').filter(Boolean).map(line=>{const rec=JSON.parse(line);return [rec.index,rec.id]}));
const canonicalIdFor=slide=>canonicalByIndex.get(p.slides.items.indexOf(slide));
p.slides.reorder([titleSlide,...contentSlides,thankYouSlide].map(canonicalIdFor));
for(let i=0;i<p.slides.items.length;i++){
  const slide=p.slides.items[i];
  const pageField=slide.shapes.items.find(s=>textOf(s).trim()==='‹#›');
  if(pageField){pageField.text='\u200B';pageField.text.style={typeface:'Verdana',fontSize:1,color:BLUE,alignment:'center',verticalAlignment:'middle',autoFit:'shrinkText',insets:0};}
  slide.shapes.add({geometry:'rect',position:{left:1198,top:668,width:58,height:38},fill:BLUE,line:{style:'solid',fill:BLUE,width:0}});
  addText(slide,String(i+1),{left:1198,top:668,width:58,height:38},{typeface:'Verdana',fontSize:14.67,color:WHITE,alignment:'center',verticalAlignment:'middle'},`footer-page-${i+1}`);
}

function textOf(shape){try{return shape.text?.toString?.()??''}catch{return ''}}
function setShapeText(shape,text,style,position){
  if(position) shape.position=position;
  shape.text=text;
  shape.text.style={typeface:'Arial',fontSize:19,color:INK,alignment:'left',verticalAlignment:'top',autoFit:'shrinkText',wrap:'square',insets:0,...style};
  return shape;
}
function addText(slide,text,position,style={},name=''){
  const sh=slide.shapes.add({geometry:'textbox',...(name?{name}:{}),position,fill:'none',line:{style:'solid',fill:'none',width:0}});
  sh.text=text;
  sh.text.style={typeface:'Arial',fontSize:19,color:INK,alignment:'left',verticalAlignment:'top',autoFit:'shrinkText',wrap:'square',insets:0,...style};
  return sh;
}
function addBox(slide,text,position,opts={}){
  const sh=slide.shapes.add({geometry:opts.geometry??'rect',...(opts.name?{name:opts.name}:{}),position,fill:opts.fill??WHITE,line:{style:'solid',fill:opts.stroke??BLUE,width:opts.lineWidth??1.5}});
  if(text){sh.text=text;sh.text.style={typeface:opts.typeface??'Arial',fontSize:opts.fontSize??18,color:opts.color??INK,bold:opts.bold??false,alignment:opts.alignment??'center',verticalAlignment:opts.verticalAlignment??'middle',autoFit:'shrinkText',wrap:'square',insets:opts.insets??8};}
  return sh;
}
function addLine(slide,x1,y1,x2,y2,color=BLUE,width=2){
  return slide.shapes.add({geometry:'line',position:{left:x1,top:y1,width:x2-x1,height:y2-y1},fill:'none',line:{style:'solid',fill:color,width}});
}
function connect(slide,from,to,fromSide='right',toSide='left',color=BLUE){
  return slide.shapes.connect(from,to,{kind:'straight',fromSide,toSide,line:{style:'solid',fill:color,width:2},tail:{type:'triangle',width:'sm',length:'sm'}});
}
function setContentSlide(slide,title,titleSize=42.67){
  const titleShape=slide.shapes.items.find(s=>s.placeholderType==='title') ?? slide.shapes.items.find(s=>textOf(s).trim()==='ABSTRACT');
  if(!titleShape) throw new Error(`No title shape on slide ${slide.index}`);
  titleShape.position={left:319.64,top:22.67,width:626,height:103.4};
  titleShape.text=title;
  titleShape.text.style={typeface:'Times New Roman',fontSize:titleSize,bold:true,color:INK,alignment:'center',verticalAlignment:'middle',autoFit:'shrinkText',wrap:'square',insets:0};
  const body=slide.shapes.items.find(s=>textOf(s).includes('Smart digital platform designed to simplify campus life.'));
  if(body) body.text='\u200B';
}
function bulletText(items){return items.map(x=>`•  ${x}`).join('\n')}
function addBulletColumn(slide,items,x,y,w,h,fontSize=18.5){
  return addText(slide,bulletText(items),{left:x,top:y,width:w,height:h},{fontSize,typeface:'Arial',color:INK,lineSpacing:1.0});
}
function addCard(slide,x,y,w,h,heading,items,{bodySize=16.8}={}){
  slide.shapes.add({geometry:'rect',position:{left:x,top:y,width:w,height:h},fill:'#FFFFFF',line:{style:'solid',fill:'#B9D6EA',width:1.2}});
  const head=slide.shapes.add({geometry:'rect',position:{left:x,top:y,width:w,height:38},fill:BLUE,line:{style:'solid',fill:BLUE,width:1}});
  head.text=heading;head.text.style={typeface:'Arial',fontSize:19,bold:true,color:WHITE,alignment:'center',verticalAlignment:'middle',autoFit:'shrinkText',insets:6};
  addText(slide,bulletText(items),{left:x+12,top:y+50,width:w-24,height:h-60},{fontSize:bodySize,typeface:'Arial',color:INK});
}

// Slide 1: preserve the title composition, logos, and footer.
{
  const title=titleSlide.shapes.items.find(s=>textOf(s).trim()==='WEBGENZ 2K26') ?? titleSlide.shapes.items.find(s=>s.placeholderType==='title');
  const details=titleSlide.shapes.items.find(s=>textOf(s).includes('IdeaTitle'));
  if(!title||!details) throw new Error('Could not resolve the title slide text');
  title.position={left:319.64,top:14,width:626,height:122};
  title.text='COLLEGE GRIEVANCE\nMANAGEMENT SYSTEM';
  title.text.style={typeface:'Times New Roman',fontSize:43,color:BLUE,bold:false,alignment:'center',verticalAlignment:'middle',autoFit:'shrinkText',wrap:'square',insets:0};
  details.position={left:106,top:300,width:830,height:230};
  details.text='Theme: Campus 2.0\nPS Category: Software\nTeam Name: [INSERT TEAM NAME]\nTeam Members: [INSERT TEAM MEMBERS]';
  details.text.style={typeface:'Times New Roman',fontSize:26,color:INK,bold:true,alignment:'left',verticalAlignment:'top',autoFit:'shrinkText',wrap:'square',insets:0};
  addText(titleSlide,'“Smart • Secure • Transparent Grievance Resolution”',{left:106,top:230,width:1000,height:48},{typeface:'Arial',fontSize:24,italic:true,color:BLUE,alignment:'left',verticalAlignment:'middle'},'subtitle-tagline');
}

// Slide 2: abstract with a concise summary and editable process path.
{
  const s=contentSlides[0];setContentSlide(s,'ABSTRACT');
  addBulletColumn(s,[
    'One portal for student grievances',
    'Secure complaint submission',
    'Admin review and classification',
    'Routing to the relevant staff / in-charge',
    'Staff progress updates and solution proof',
    'Admin verification before closure',
    'Traceable complaint history',
    'Role-based access for each user'
  ],42,151,615,468,18.2);
  addText(s,'GRIEVANCE RESOLUTION PATH',{left:712,top:142,width:460,height:32},{fontSize:17,bold:true,color:BLUE,alignment:'center',verticalAlignment:'middle'},'abstract-flow-title');
  const steps=['STUDENT','COMPLAINT','ADMIN REVIEW','STAFF / IN-CHARGE','SOLUTION','ADMIN VERIFICATION','RESOLVED'];
  const nodes=steps.map((t,i)=>addBox(s,t,{left:770,top:182+i*62,width:340,height:48},{fill:PALE,fontSize:17,bold:true,name:`abstract-step-${i+1}`}));
  for(let i=0;i<nodes.length-1;i++) connect(s,nodes[i],nodes[i+1],'bottom','top');
}

// Slide 3: need statement and a current/proposed comparison.
{
  const s=contentSlides[1];setContentSlide(s,'WHY IS A GRIEVANCE MANAGEMENT SYSTEM NEEDED?',35.5);
  addBulletColumn(s,[
    'Manual handling makes complaints hard to track',
    'Students may not know whom to approach',
    'Complaints can be delayed or lost',
    'Students need clear status visibility'
  ],54,145,550,205,17.5);
  addBulletColumn(s,[
    'Admins need a view of pending and resolved issues',
    'Staff need complaints routed by stream',
    'Solution verification needs a recorded trail'
  ],650,145,565,205,17.5);
  addText(s,'CURRENT / MANUAL',{left:54,top:374,width:540,height:28},{fontSize:19,bold:true,color:BLUE,alignment:'left',verticalAlignment:'middle'});
  addText(s,'PROPOSED SYSTEM',{left:650,top:374,width:565,height:28},{fontSize:19,bold:true,color:BLUE,alignment:'left',verticalAlignment:'middle'});
  addBox(s,'Complaint  →  Person  →  Follow-up  →  Unclear status',{left:54,top:411,width:540,height:92},{fill:'#F7F7F7',stroke:'#A8B7C1',fontSize:18.5,bold:true,alignment:'center'});
  addBox(s,'Complaint  →  Admin  →  Assigned staff  →  Solution  →  Verification  →  Resolved',{left:650,top:411,width:565,height:92},{fill:PALE,stroke:BLUE,fontSize:18,bold:true,alignment:'center'});
  addText(s,'Centralized review, routing and verification make each step visible.',{left:180,top:535,width:920,height:42},{fontSize:19,bold:true,color:INK,alignment:'center',verticalAlignment:'middle'});
}

// Slide 4: importance as clean paired points.
{
  const s=contentSlides[2];setContentSlide(s,'IMPORTANCE OF OUR WEBSITE');
  const points=[
    'Centralized grievance management','Faster complaint routing',
    'Transparent complaint status','Secure student authentication',
    'Role-based access','Proper staff assignment',
    'Proof-based resolution','Complaint history and audit trail',
    'Reduced manual communication','Better student-administration interaction'
  ];
  for(let i=0;i<points.length;i++){
    const col=i<5?0:1,row=i%5,x=70+col*600,y=152+row*88;
    addBox(s,'',{left:x,top:y+13,width:12,height:12},{fill:BLUE,stroke:BLUE,lineWidth:1});
    addText(s,points[i],{left:x+28,top:y,width:535,height:42},{fontSize:20.5,typeface:'Arial',color:INK,verticalAlignment:'middle'});
    if(row<4) addLine(s,x+28,y+57,x+550,y+57,'#D9E7F0',1);
  }
}

// Slide 5: role-based sections.
{
  const s=contentSlides[3];setContentSlide(s,'USER ROLES');
  addCard(s,38,151,382,466,'STUDENT',[
    'Register and verify email with OTP','Login with Roll ID and password','Submit grievances','Upload optional supporting proof','Track status and view complaint history','Edit before admin acceptance'
  ],{bodySize:16.6});
  addCard(s,449,151,382,466,'STAFF / IN-CHARGE',[
    'View assigned complaints','Start complaint resolution','Add remarks','Submit a solution','Upload solution proof','Update assigned complaint progress'
  ],{bodySize:16.6});
  addCard(s,860,151,382,466,'ADMIN',[
    'Approve or reject student registrations','Review and accept or reject complaints','Classify streams and assign staff','Verify solutions and reopen unresolved complaints','Manage students, staff, streams and academic years','Monitor grievance progress'
  ],{bodySize:16.0});
}

// Slide 6: full flow plus status lifecycle, all as editable shapes.
{
  const s=contentSlides[4];setContentSlide(s,'FLOWCHART / WORKFLOW');
  const xs=[35,210,385,560,735,910,1085], w=160, h=44;
  const r1=['STUDENT','REGISTER','EMAIL OTP\nVERIFICATION','ADMIN\nAPPROVAL','STUDENT LOGIN','SUBMIT GRIEVANCE','ADMIN REVIEW'];
  const top=r1.map((t,i)=>addBox(s,t,{left:xs[i],top:139,width:w,height:h},{fill:PALE,fontSize:15.2,bold:true,name:`flow-top-${i+1}`}));
  for(let i=0;i<top.length-1;i++)connect(s,top[i],top[i+1]);
  const row2=[
    {x:1085,t:'VALID?'},{x:910,t:'CLASSIFY\nSTREAM'},{x:735,t:'ASSIGN STAFF /\nIN-CHARGE'},
    {x:560,t:'STAFF WORKS ON\nCOMPLAINT'},{x:385,t:'SOLUTION +\nOPTIONAL PROOF'},{x:210,t:'ADMIN\nVERIFICATION'}
  ];
  const mid=row2.map((n,i)=>addBox(s,n.t,{left:n.x,top:207,width:w,height:h},{fill:WHITE,fontSize:14.8,bold:true,name:`flow-mid-${i+1}`}));
  connect(s,top[6],mid[0],'bottom','top');
  for(let i=0;i<mid.length-1;i++)connect(s,mid[i],mid[i+1],'left','right');
  addText(s,'YES',{left:1013,top:224,width:45,height:20},{fontSize:13,bold:true,color:BLUE,alignment:'center',verticalAlignment:'middle'});
  const solved=addBox(s,'SOLVED?',{left:210,top:275,width:w,height:h},{fill:'#FFF9E9',stroke:'#C79326',fontSize:15.5,bold:true,name:'flow-solved'});
  const reopen=addBox(s,'REOPENED',{left:385,top:275,width:w,height:h},{fill:'#FFF3F0',stroke:'#C75A46',fontSize:15,bold:true,name:'flow-reopened'});
  const inProgress=addBox(s,'IN_PROGRESS',{left:560,top:275,width:w,height:h},{fill:'#FFF9E9',stroke:'#C79326',fontSize:15,bold:true,name:'flow-in-progress'});
  const resolved=addBox(s,'RESOLVED',{left:735,top:275,width:w,height:h},{fill:'#EFF8F0',stroke:'#45854B',fontSize:15,bold:true,name:'flow-resolved'});
  const rejected=addBox(s,'REJECTION MESSAGE\nTO STUDENT',{left:1085,top:275,width:w,height:h},{fill:'#FFF3F0',stroke:'#C75A46',fontSize:13.8,bold:true,name:'flow-rejection'});
  connect(s,mid[5],solved,'bottom','top');
  connect(s,solved,reopen,'right','left');connect(s,reopen,inProgress,'right','left');
  connect(s,mid[0],rejected,'bottom','top');
  addText(s,'NO',{left:1117,top:253,width:35,height:18},{fontSize:12.5,bold:true,color:'#A23A2B',alignment:'center',verticalAlignment:'middle'});
  addText(s,'NO',{left:324,top:288,width:36,height:18},{fontSize:12.5,bold:true,color:'#A23A2B',alignment:'center',verticalAlignment:'middle'});
  addText(s,'YES  →  RESOLVED',{left:760,top:326,width:210,height:22},{fontSize:13.5,bold:true,color:'#45854B',alignment:'center',verticalAlignment:'middle'});
  addText(s,'STATUS LIFECYCLE',{left:42,top:365,width:250,height:28},{fontSize:18,bold:true,color:BLUE,verticalAlignment:'middle'});
  const status=['REGISTERED','ACCEPTED','ASSIGNED','IN_PROGRESS','SOLUTION_SUBMITTED','SOLVED'];
  const sw=170,sg=22,sx=42,sy=407;
  const statusNodes=status.map((t,i)=>addBox(s,t,{left:sx+i*(sw+sg),top:sy,width:sw,height:52},{fill:PALE,fontSize:14.8,bold:true,name:`status-${i+1}`}));
  for(let i=0;i<statusNodes.length-1;i++)connect(s,statusNodes[i],statusNodes[i+1]);
  addText(s,'REOPEN BRANCH   SOLUTION_SUBMITTED  →  REOPENED  →  IN_PROGRESS',{left:276,top:487,width:740,height:46},{fontSize:17.3,bold:true,color:INK,alignment:'center',verticalAlignment:'middle'});
}

// Slide 7: requested grievance streams.
{
  const s=contentSlides[5];setContentSlide(s,'GRIEVANCE STREAMS');
  addCard(s,62,153,548,216,'ACADEMIC',['Faculty issues','Marks','Attendance','Exams','Syllabus','Timetable'],{bodySize:17});
  addCard(s,670,153,548,216,'FACULTY & TEACHING',['Faculty behaviour','Teaching quality','Unfair treatment'],{bodySize:17.5});
  addCard(s,62,394,548,216,'FEES & FINANCE',['Fees','Refunds','Scholarships','Fines'],{bodySize:17.5});
  addCard(s,670,394,548,216,'INFRASTRUCTURE',['Classrooms','Labs and computers','Wi-Fi and electricity','Furniture'],{bodySize:17.5});
}

// Slide 8: security and authentication blocks.
{
  const s=contentSlides[6];setContentSlide(s,'SECURITY & AUTHENTICATION');
  const groups=[
    ['AUTHENTICATION',['JWT-based authentication','Email OTP verification','Student login: Roll ID + password']],
    ['ACCESS CONTROL',['Spring Security','Role-based authorization','Staff / Admin role protection']],
    ['DATA PROTECTION',['BCrypt password hashing','Secure profile responses','Passwords and hashes never exposed']],
    ['BACKEND CHECKS',['Backend-enforced permissions','Student complaint ownership checks','Complaint operations validated on backend']]
  ];
  const positions=[[55,153],[655,153],[55,384],[655,384]];
  groups.forEach((g,i)=>addCard(s,positions[i][0],positions[i][1],570,205,g[0],g[1],{bodySize:17}));
}

// Slide 9: five clean, empty student screenshot placeholders.
function addPlaceholder(slide,x,y,label,alt,w=376,h=210){
  const frame=slide.shapes.add({geometry:'rect',position:{left:x,top:y,width:w,height:h},fill:'#F5F9FC',line:{style:'solid',fill:BLUE,width:2}});
  frame.name=`screenshot-placeholder-${alt.toLowerCase().replaceAll(' ','-')}`;
  addText(slide,`${label}\n[ INSERT ${alt.toUpperCase()} SCREENSHOT HERE ]`,{left:x+12,top:y+52,width:w-24,height:h-88},{fontSize:15.8,bold:true,color:GREY,alignment:'center',verticalAlignment:'middle'},`placeholder-label-${alt.toLowerCase().replaceAll(' ','-')}`);
}
{
  const s=contentSlides[7];setContentSlide(s,'WEBSITE SCREENS / USER INTERFACE',35.5);
  const w=376,h=210,topY=151,bottomY=385;
  [
    [54,topY,'LOGIN PAGE','LOGIN PAGE'],[452,topY,'STUDENT REGISTRATION','STUDENT REGISTRATION'],[850,topY,'STUDENT DASHBOARD','STUDENT DASHBOARD'],
    [253,bottomY,'STUDENT COMPLAINT PAGE','STUDENT COMPLAINT'],[651,bottomY,'STUDENT COMPLAINT DETAILS','STUDENT COMPLAINT DETAILS']
  ].forEach(a=>addPlaceholder(s,...a,w,h));
}

// Slide 10: six clean, empty admin, staff, and related screenshot placeholders.
{
  const s=contentSlides[8];setContentSlide(s,'ADMIN & STAFF MODULES');
  const w=376,h=210,topY=151,bottomY=385;
  [
    [54,topY,'ADMIN DASHBOARD','ADMIN DASHBOARD'],[452,topY,'ADMIN COMPLAINT MANAGEMENT','ADMIN COMPLAINT MANAGEMENT'],[850,topY,'STAFF DASHBOARD','STAFF DASHBOARD'],
    [54,bottomY,'STAFF COMPLAINT / SOLUTION','STAFF COMPLAINT / SOLUTION'],[452,bottomY,'STUDENT PROFILE','STUDENT PROFILE'],[850,bottomY,'ADMIN / STAFF MANAGEMENT','ADMIN / STAFF MANAGEMENT']
  ].forEach(a=>addPlaceholder(s,...a,w,h));
}

// Slide 11: additional website features.
{
  const s=contentSlides[9];setContentSlide(s,'ADDITIONAL WEBSITE FEATURES');
  addCard(s,38,151,382,466,'STUDENT INTAKE',[
    'Registration with email OTP','Admin registration approval','Roll ID-based authentication','Grievance submission','Responsive interface'
  ],{bodySize:16.5});
  addCard(s,449,151,382,466,'COMPLAINT HANDLING',[
    'Stream classification','Staff assignment','Complaint status tracking','Solution submission','Proof / document upload'
  ],{bodySize:16.5});
  addCard(s,860,151,382,466,'ADMINISTRATION',[
    'Admin solution verification','Complaint reopening','Complaint history','Academic-year management','Student / Staff / Admin profiles'
  ],{bodySize:16.5});
}

// Slide 12: visual technology stack.
{
  const s=contentSlides[10];setContentSlide(s,'TECHNOLOGY STACK');
  const front=addBox(s,'FRONTEND\nReact   •   Vite   •   React Router\nAxios   •   Responsive CSS',{left:94,top:143,width:1092,height:118},{fill:PALE,fontSize:21,bold:true,name:'technology-frontend'});
  const api=addBox(s,'REST APIs',{left:496,top:278,width:290,height:48},{fill:WHITE,fontSize:19,bold:true,name:'technology-api'});
  const backend=addBox(s,'BACKEND\nJava   •   Spring Boot   •   Spring Security\nJWT   •   Spring Data JPA',{left:94,top:345,width:1092,height:122},{fill:PALE,fontSize:21,bold:true,name:'technology-backend'});
  const db=addBox(s,'DATABASE\nMySQL',{left:94,top:500,width:335,height:94},{fill:WHITE,fontSize:20,bold:true,name:'technology-database'});
  const otp=addBox(s,'EMAIL OTP\nSERVICE',{left:472,top:500,width:335,height:94},{fill:WHITE,fontSize:20,bold:true,name:'technology-otp'});
  const upload=addBox(s,'FILE UPLOAD /\nPROOF HANDLING',{left:850,top:500,width:335,height:94},{fill:WHITE,fontSize:20,bold:true,name:'technology-upload'});
  connect(s,front,api,'bottom','top');connect(s,api,backend,'bottom','top');
  connect(s,backend,db,'bottom','top');connect(s,backend,otp,'bottom','top');connect(s,backend,upload,'bottom','top');
}

// Slide 13: system architecture.
{
  const s=contentSlides[11];setContentSlide(s,'SYSTEM ARCHITECTURE');
  const user=addBox(s,'STUDENT / STAFF / ADMIN',{left:395,top:133,width:490,height:48},{fill:PALE,fontSize:18,bold:true,name:'architecture-users'});
  const ui=addBox(s,'REACT UI',{left:395,top:195,width:490,height:48},{fill:WHITE,fontSize:18,bold:true,name:'architecture-ui'});
  const api=addBox(s,'REST API LAYER',{left:395,top:257,width:490,height:48},{fill:PALE,fontSize:18,bold:true,name:'architecture-api'});
  const backend=addBox(s,'SPRING BOOT BACKEND',{left:395,top:319,width:490,height:52},{fill:WHITE,fontSize:18,bold:true,name:'architecture-backend'});
  connect(s,user,ui,'bottom','top');connect(s,ui,api,'bottom','top');connect(s,api,backend,'bottom','top');
  const sec=addBox(s,'SECURITY\nJWT / BCrypt',{left:91,top:392,width:290,height:74},{fill:PALE,fontSize:16.8,bold:true,name:'architecture-security'});
  const logic=addBox(s,'BUSINESS LOGIC',{left:495,top:392,width:290,height:74},{fill:PALE,fontSize:16.8,bold:true,name:'architecture-logic'});
  const file=addBox(s,'FILE HANDLING',{left:899,top:392,width:290,height:74},{fill:PALE,fontSize:16.8,bold:true,name:'architecture-file'});
  connect(s,backend,sec,'bottom','top');connect(s,backend,logic,'bottom','top');connect(s,backend,file,'bottom','top');
  const jpa=addBox(s,'JPA / HIBERNATE',{left:395,top:488,width:490,height:48},{fill:WHITE,fontSize:18,bold:true,name:'architecture-jpa'});
  const mysql=addBox(s,'MYSQL',{left:515,top:557,width:250,height:48},{fill:PALE,fontSize:18,bold:true,name:'architecture-mysql'});
  connect(s,logic,jpa,'bottom','top');connect(s,jpa,mysql,'bottom','top');
  const otp=addBox(s,'EMAIL OTP SERVICE',{left:80,top:319,width:270,height:58},{fill:WHITE,fontSize:16.5,bold:true,name:'architecture-otp'});
  connect(s,backend,otp,'left','right');
}

// Slide 14: simplified data relationships.
{
  const s=contentSlides[12];setContentSlide(s,'DATA MANAGEMENT');
  const student=addBox(s,'STUDENT\nSubmits complaints',{left:45,top:151,width:208,height:84},{fill:PALE,fontSize:16.3,bold:true,name:'entity-student'});
  const year=addBox(s,'ACADEMIC YEAR\nStudents / complaints',{left:285,top:151,width:208,height:84},{fill:PALE,fontSize:15.8,bold:true,name:'entity-year'});
  const admin=addBox(s,'ADMIN\nReviews / assigns / verifies',{left:525,top:151,width:208,height:84},{fill:PALE,fontSize:15.8,bold:true,name:'entity-admin'});
  const staff=addBox(s,'STAFF\nReceives assigned complaints',{left:765,top:151,width:208,height:84},{fill:PALE,fontSize:15.8,bold:true,name:'entity-staff'});
  const stream=addBox(s,'STREAM\nComplaint classification',{left:1005,top:151,width:208,height:84},{fill:PALE,fontSize:15.8,bold:true,name:'entity-stream'});
  const complaint=addBox(s,'REGISTERED COMPLAINT',{left:423,top:334,width:434,height:92},{fill:WHITE,fontSize:21,bold:true,name:'entity-complaint'});
  const history=addBox(s,'COMPLAINT HISTORY',{left:500,top:514,width:280,height:72},{fill:PALE,fontSize:19,bold:true,name:'entity-history'});
  connect(s,student,complaint,'bottom','top');connect(s,year,complaint,'bottom','top');connect(s,admin,complaint,'bottom','top');connect(s,staff,complaint,'bottom','top');connect(s,stream,complaint,'bottom','top');connect(s,complaint,history,'bottom','top');
}

// Slide 15: complaint lifecycle with a reopen branch.
{
  const s=contentSlides[13];setContentSlide(s,'COMPLAINT LIFECYCLE');
  const states=[
    ['REGISTERED','#EAF4FB',BLUE],['ACCEPTED','#EAF4FB',BLUE],['ASSIGNED','#EAF4FB',BLUE],
    ['IN PROGRESS','#FFF7E6','#C79326'],['SOLUTION\nSUBMITTED','#FFF7E6','#C79326'],
    ['ADMIN\nVERIFICATION','#F3F8FC',BLUE],['SOLVED','#EFF8F0','#45854B']
  ];
  const x0=34,w=158,gap=19,y=227,h=86;
  const nodes=states.map((v,i)=>addBox(s,v[0],{left:x0+i*(w+gap),top:y,width:w,height:h},{fill:v[1],stroke:v[2],fontSize:15.5,bold:true,name:`lifecycle-${i+1}`}));
  for(let i=0;i<nodes.length-1;i++)connect(s,nodes[i],nodes[i+1]);
  addText(s,'REOPEN BRANCH',{left:72,top:375,width:220,height:30},{fontSize:18,bold:true,color:BLUE,verticalAlignment:'middle'});
  const submitted=addBox(s,'SOLUTION\nSUBMITTED',{left:315,top:424,width:220,height:82},{fill:'#FFF7E6',stroke:'#C79326',fontSize:16.5,bold:true,name:'lifecycle-reopen-submitted'});
  const reopened=addBox(s,'REOPENED',{left:552,top:424,width:220,height:82},{fill:'#FFF3F0',stroke:'#C75A46',fontSize:17,bold:true,name:'lifecycle-reopened'});
  const progress=addBox(s,'IN PROGRESS',{left:789,top:424,width:220,height:82},{fill:'#FFF7E6',stroke:'#C79326',fontSize:17,bold:true,name:'lifecycle-reopened-progress'});
  connect(s,submitted,reopened);connect(s,reopened,progress);
  addText(s,'Admin reopens the complaint when the submitted solution does not resolve it.',{left:208,top:536,width:860,height:40},{fontSize:17.5,color:INK,alignment:'center',verticalAlignment:'middle'});
}

// Slide 16: future scope, explicitly separated from implemented features.
{
  const s=contentSlides[14];setContentSlide(s,'FUTURE ENHANCEMENTS');
  const tag=addBox(s,'FUTURE SCOPE',{left:493,top:136,width:294,height:42},{fill:BLUE,stroke:BLUE,fontSize:19,bold:true,color:WHITE,name:'future-scope-label'});
  const items=[
    'Email / SMS status notifications','Analytics dashboard','Complaint trend analysis',
    'SLA and escalation management','Advanced search and filtering','Mobile application',
    'Automated complaint categorization','AI-assisted grievance prioritization','More detailed reporting'
  ];
  const cols=[items.slice(0,3),items.slice(3,6),items.slice(6,9)];
  cols.forEach((col,i)=>addCard(s,38+i*411,205,382,365,['NOTIFICATIONS & INSIGHT','WORKFLOW TOOLS','AUTOMATION & REPORTING'][i],col,{bodySize:17}));
  addText(s,'These are future enhancements, not current system features.',{left:220,top:595,width:840,height:30},{fontSize:16.5,italic:true,color:GREY,alignment:'center',verticalAlignment:'middle'});
}

// Slide 17: original Thank You style, with team placeholders.
{
  const title=thankYouSlide.shapes.items.find(s=>s.placeholderType==='title') ?? thankYouSlide.shapes.items.find(s=>textOf(s).trim()==='THANK YOU SLIDE');
  const body=thankYouSlide.shapes.items.find(s=>textOf(s).includes('CampusConnect'));
  if(!title||!body) throw new Error('Could not resolve the Thank You slide text');
  title.text='THANK YOU';
  title.position={left:319.64,top:22.67,width:626,height:72};
  title.text.style={typeface:'Times New Roman',fontSize:42.67,bold:true,color:INK,alignment:'center',verticalAlignment:'middle',autoFit:'shrinkText',insets:0};
  body.text='\u200B';
  addText(thankYouSlide,'COLLEGE GRIEVANCE MANAGEMENT SYSTEM',{left:255,top:194,width:770,height:45},{fontSize:24,bold:true,color:INK,alignment:'center',verticalAlignment:'middle'},'thank-project');
  addText(thankYouSlide,'“Transparent Grievances. Faster Resolution. Better Campus.”',{left:215,top:254,width:850,height:42},{fontSize:20,italic:true,color:INK,alignment:'center',verticalAlignment:'middle'},'thank-tagline');
  addText(thankYouSlide,'Team Name: [INSERT TEAM NAME]',{left:295,top:337,width:690,height:38},{fontSize:19,bold:true,color:INK,alignment:'center',verticalAlignment:'middle'},'thank-team');
  addText(thankYouSlide,'Team Members: [INSERT TEAM MEMBERS]',{left:295,top:386,width:690,height:42},{fontSize:19,color:INK,alignment:'center',verticalAlignment:'middle'},'thank-members');
}

if(p.slides.items.length!==17) throw new Error(`Expected 17 slides after assembly, got ${p.slides.items.length}`);
await fs.mkdir(buildDir,{recursive:true});
const pptx=await PresentationFile.exportPptx(p);await pptx.save(`${buildDir}/candidate.pptx`);
const montage=await p.export({format:'png',montage:{columns:3,slideWidth:480,gap:14,padding:14},scale:1});
await fs.writeFile(`${buildDir}/contact-sheet.png`,new Uint8Array(await montage.arrayBuffer()));
for(let i=0;i<p.slides.items.length;i++){
  const png=await p.export({slide:p.slides.items[i],format:'png',scale:1});
  await fs.writeFile(`${buildDir}/slide-${String(i+1).padStart(2,'0')}.png`,new Uint8Array(await png.arrayBuffer()));
}
const inspect=await p.inspect({select:'$.slides[*]',include:['id','index','title','elements.kind','elements.text','elements.position','elements.style']});
await fs.writeFile(`${buildDir}/deck-inspect.ndjson`,inspect.ndjson);
const sha=createHash('sha256').update(await fs.readFile(sourcePath)).digest('hex');
console.log(JSON.stringify({slides:p.slides.items.length,candidate:`${buildDir}/candidate.pptx`,montage:`${buildDir}/contact-sheet.png`,sourceSha256:sha,slideSize:p.slides.items[0].frame},null,2));
