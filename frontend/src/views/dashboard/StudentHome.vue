<script setup>
const labs = [
  {
    name: '软件工程实验室',
    location: '科技楼 A301',
    purpose: '用于软件开发、课程实验与项目训练。',
    equipment: '开发工作站、激光投影仪',
    tip: '使用结束后请退出个人账号，并整理桌面设备。'
  },
  {
    name: '人工智能实验室',
    location: '科技楼 B405',
    purpose: '配备 GPU 工作站，支持机器学习实验。',
    equipment: '深度学习服务器',
    tip: '运行任务前确认 GPU 与存储空间使用情况，不要结束他人任务。'
  },
  {
    name: '物联网创新实验室',
    location: '实验楼 C208',
    purpose: '用于嵌入式、传感器和物联网实践。',
    equipment: '物联网实验箱',
    tip: '接线前先断电，确认接线正确后再通电。'
  },
  {
    name: '机器人与智能制造实验室',
    location: '实验楼 D212',
    purpose: '用于机器人控制与智能制造实践。',
    equipment: '设备以实验室设备台账为准。',
    tip: '使用前确认设备状态，并按实验指导书操作。'
  },
  {
    name: '网络安全实验室',
    location: '科技楼 E503',
    purpose: '用于网络安全教学和攻防实验。',
    equipment: '设备以实验室设备台账为准。',
    tip: '仅在授权环境中开展实验，不要扫描或测试未授权网络。'
  }
]

const equipment = [
  {
    name: '开发工作站',
    steps: [
      '使用分配的实验室账号登录。',
      '实验文件保存在个人目录中。',
      '不要随意安装未经允许的软件。',
      '使用结束后保存文件并正常退出账号。',
      '最后一位使用者按照实验室要求关闭设备。'
    ]
  },
  {
    name: '深度学习服务器',
    steps: [
      '使用教师或管理员分配的账号连接服务器。',
      '数据集和实验文件上传到指定目录。',
      '运行任务前检查 GPU 和存储空间占用。',
      '不要结束其他用户的进程。',
      '实验结束后及时关闭无用任务并清理临时文件。'
    ]
  },
  {
    name: '物联网实验箱',
    steps: [
      '接线前确认实验箱处于断电状态。',
      '按照实验指导书连接传感器和控制模块。',
      '检查接线正确后再接通电源。',
      '不要带电插拔模块。',
      '实验结束后断电，并将模块恢复原位。'
    ]
  },
  {
    name: '激光投影仪',
    steps: [
      '打开投影仪并等待设备启动。',
      '使用 HDMI 或无线投屏方式连接电脑。',
      '选择正确的输入信号源。',
      '使用结束后正常关机。',
      '投影仪散热期间不要直接切断总电源。'
    ]
  }
]

const notices = [
  '进入实验室后遵守实验室管理规定。',
  '只使用本人预约并审核通过的时间段。',
  '未经允许不得移动、拆卸或带走设备。',
  '发现设备异常时停止使用，并通过“设备报修”功能提交问题。',
  '实验结束后关闭设备、整理桌面并带走个人物品。'
]
</script>

<template>
  <div class="guide-page">
    <header class="guide-header">
      <div class="guide-kicker">LABORATORY GUIDE</div>
      <h1>实验室指南</h1>
      <p>了解实验室环境与设备使用方法，安全、规范地开展实验。</p>
      <div class="guide-actions">
        <router-link class="guide-button primary" to="/reservations"><Calendar />预约实验室</router-link>
        <router-link class="guide-button secondary" to="/repairs"><Tools />设备报修</router-link>
      </div>
    </header>

    <section class="guide-section">
      <div class="section-title">
        <div><span>01</span><h2>实验室介绍</h2></div>
        <p>预约前了解实验室位置、用途与使用提示</p>
      </div>
      <div class="lab-grid">
        <article v-for="lab in labs" :key="lab.name" class="lab-card">
          <div class="lab-card-top"><span class="lab-icon"><OfficeBuilding /></span><span class="lab-label">实验空间</span></div>
          <h3>{{ lab.name }}</h3>
          <p class="location"><Location />{{ lab.location }}</p>
          <p class="purpose">{{ lab.purpose }}</p>
          <div class="lab-detail"><b>主要设备</b><span>{{ lab.equipment }}</span></div>
          <div class="lab-tip"><InfoFilled /><span><b>使用提示：</b>{{ lab.tip }}</span></div>
        </article>
      </div>
    </section>

    <section class="guide-section equipment-section">
      <div class="section-title">
        <div><span>02</span><h2>常用设备使用方法</h2></div>
        <p>按要求操作设备，使用完毕后及时恢复现场</p>
      </div>
      <div class="equipment-grid">
        <article v-for="(item, index) in equipment" :key="item.name" class="equipment-card">
          <div class="equipment-title"><span>{{ String(index + 1).padStart(2, '0') }}</span><h3>{{ item.name }}</h3></div>
          <ol><li v-for="step in item.steps" :key="step">{{ step }}</li></ol>
        </article>
      </div>
    </section>

    <section class="notice-panel">
      <div class="notice-heading"><span class="notice-icon"><Warning /></span><div><h2>实验室使用须知</h2><p>安全、规范使用实验资源，共同维护实验环境</p></div></div>
      <ul><li v-for="notice in notices" :key="notice">{{ notice }}</li></ul>
    </section>
  </div>
</template>

<style scoped>
.guide-page{max-width:1440px;margin:0 auto;padding:30px 32px 42px;color:var(--ink)}
.guide-header{padding:30px 34px;border-radius:16px;background:linear-gradient(112deg,#eef8f8,#f2f7fb);border:1px solid #dfeded}
.guide-kicker{font-size:11px;letter-spacing:1.8px;font-weight:700;color:var(--primary)}
.guide-header h1{margin:8px 0;font-size:28px;color:#24454c}
.guide-header p{margin:0;color:#71868b;font-size:14px}
.guide-actions{display:flex;gap:10px;margin-top:22px}
.guide-button{display:inline-flex;align-items:center;gap:8px;padding:10px 16px;border-radius:8px;text-decoration:none;font-size:13px;font-weight:600;transition:background .2s,transform .2s}
.guide-button:hover{transform:translateY(-1px)}
.guide-button svg{width:16px}
.guide-button.primary{background:var(--primary);color:#fff}
.guide-button.primary:hover{background:var(--primary-dark)}
.guide-button.secondary{background:#fff;border:1px solid #d5e7e8;color:#347981}
.guide-button.secondary:hover{background:#f8fcfc}
.guide-section{margin-top:30px}
.section-title{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:15px}
.section-title>div{display:flex;align-items:center;gap:11px}
.section-title>div>span{color:#48a1a4;font-size:12px;font-weight:700}
.section-title h2{margin:0;font-size:19px;color:#2c4b51}
.section-title>p{margin:0;color:#87999d;font-size:12px}
.lab-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:15px}
.lab-card,.equipment-card{background:#fff;border:1px solid var(--border);border-radius:12px;box-shadow:0 4px 16px #1a718008}
.lab-card{padding:19px 20px}
.lab-card-top{display:flex;align-items:center;justify-content:space-between}
.lab-icon{display:grid;place-items:center;width:38px;height:38px;border-radius:10px;background:#e8f6f5;color:var(--primary)}
.lab-icon svg{width:19px}
.lab-label{padding:5px 8px;border-radius:20px;background:#f2f7f8;color:#7a9095;font-size:10px}
.lab-card h3{margin:14px 0 8px;font-size:16px;color:#2c484e}
.location{display:flex;align-items:center;gap:5px;margin:0;color:#668087;font-size:12px}
.location svg{width:14px;color:#3c969b}
.purpose{min-height:38px;margin:13px 0;color:#687f84;font-size:13px;line-height:1.65}
.lab-detail{display:flex;flex-direction:column;gap:5px;padding:11px 0;border-top:1px solid #edf2f3;color:#627b80;font-size:12px}
.lab-detail b{color:#3e6066}
.lab-tip{display:flex;align-items:flex-start;gap:7px;padding:9px 10px;border-radius:7px;background:#f2f9f8;color:#647e82;font-size:11px;line-height:1.55}
.lab-tip svg{flex:none;width:14px;margin-top:2px;color:#4a9b9e}
.lab-tip b{color:#397b80}
.equipment-section{margin-top:32px}
.equipment-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px}
.equipment-card{padding:18px 20px}
.equipment-title{display:flex;align-items:center;gap:10px}
.equipment-title>span{color:#4a9fa2;font-size:11px;font-weight:700}
.equipment-title h3{margin:0;color:#2c4b51;font-size:15px}
.equipment-card ol{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px 18px;margin:15px 0 0;padding-left:20px;color:#6e8387;font-size:12px;line-height:1.6}
.equipment-card li{padding-left:2px}
.notice-panel{margin-top:30px;padding:20px 23px;border:1px solid #e3ecee;border-radius:12px;background:#fff}
.notice-heading{display:flex;align-items:center;gap:11px}
.notice-icon{display:grid;place-items:center;width:38px;height:38px;border-radius:10px;background:#fff5e8;color:#d79142}
.notice-icon svg{width:18px}
.notice-heading h2{margin:0;color:#2c4b51;font-size:17px}
.notice-heading p{margin:4px 0 0;color:#87999d;font-size:11px}
.notice-panel ul{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:9px 24px;margin:18px 0 0;padding-left:19px;color:#687f84;font-size:12px;line-height:1.6}
.notice-panel li::marker{color:#3c9a9c}
@media(max-width:1050px){.lab-grid{grid-template-columns:repeat(2,minmax(0,1fr))}}
@media(max-width:700px){.guide-page{padding:20px 15px 30px}.guide-header{padding:24px 21px}.lab-grid,.equipment-grid{grid-template-columns:1fr}.section-title{align-items:flex-start;gap:8px;flex-direction:column}.equipment-card ol,.notice-panel ul{grid-template-columns:1fr}.lab-card{padding:17px}}
</style>
