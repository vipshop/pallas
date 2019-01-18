import Vue from 'vue';
import Router from 'vue-router';
import RouterMapper from './utils/router_mapper';
import Login from './Login';
import IndexManage from './pages/index_manage/index_manage';
import IndexDetail from './pages/index_detail/index_detail';
import VersionManage from './pages/index_detail/version_manage/version_manage';
import TemplateManage from './pages/index_detail/template_manage/template_manage';
import DynamicManage from './pages/index_detail/dynamic_manage/dynamic_manage';
import FlowRecord from './pages/index_detail/flow_record/flow_record';
import CronDelete from './pages/index_detail/cron_delete/cron_delete_manage';
import RouteManage from './pages/index_detail/route_manage/route_manage';
import ServiceManage from './pages/index_detail/service_manage/service_manage';
import ClusterManage from './pages/cluster_manage/cluster_manage';
import ClusterRouteManage from './pages/cluster_manage/cluster_route_manage/cluster_route_manage';
import ClusterNodeRestart from './pages/cluster_manage/cluster_node_restart/cluster_node_restart';
import ClusterDetail from './pages/cluster_detail/cluster_detail';
import AuthorityManage from './pages/authority_manage/authority_manage';
import AuthorityManageAdministrator from './pages/authority_manage/authority_manage_administrator';
import PluginManage from './pages/plugin_manage/plugin_manage';
import PluginUpgrade from './pages/plugin_manage/plugin_upgrade';
import AgentManage from './pages/agent_manage/agent_manage';
import TokenManage from './pages/token_manage/token_manage';
import PermissionManage from './pages/permission_manage/permission_manage';
import UserManage from './pages/permission_manage/user_manage/user_manage';

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: RouterMapper.GetPath('indexManage'),
      name: 'index_manage',
      component: IndexManage,
    }, {
      path: '/',
      redirect: 'index_manage',
    }, {
      path: RouterMapper.GetPath('login'),
      name: 'login',
      component: Login,
    }, {
      path: RouterMapper.GetPath('indexDetail'),
      component: IndexDetail,
      children: [
        { path: '', redirect: 'version_manage' },
        { name: 'version_manage', path: RouterMapper.GetPath('versionManage'), component: VersionManage },
        { name: 'template_manage', path: RouterMapper.GetPath('templateManage'), component: TemplateManage },
        { name: 'dynamic_manage', path: RouterMapper.GetPath('dynamicManage'), component: DynamicManage },
        { name: 'route_manage', path: RouterMapper.GetPath('routeManage'), component: RouteManage },
        { name: 'service_manage', path: RouterMapper.GetPath('serviceManage'), component: ServiceManage },
        { name: 'flow_record', path: RouterMapper.GetPath('flowRecord'), component: FlowRecord },
        { name: 'cron_delete', path: RouterMapper.GetPath('cronDelete'), component: CronDelete },
      ],
    }, {
      path: RouterMapper.GetPath('clusterManage'),
      name: 'cluster_manage',
      component: ClusterManage,
    }, {
      path: RouterMapper.GetPath('clusterRouteManage'),
      name: 'cluster_route_manage',
      component: ClusterRouteManage,
    }, {
      path: RouterMapper.GetPath('clusterNodeRestart'),
      name: 'cluster_node_restart',
      component: ClusterNodeRestart,
    }, {
      path: RouterMapper.GetPath('clusterDetail'),
      name: 'cluster_detail',
      component: ClusterDetail,
    }, {
      path: RouterMapper.GetPath('authorityManage'),
      name: 'authority_manage',
      component: AuthorityManage,
    }, {
      path: RouterMapper.GetPath('authorityManageAdministrator'),
      name: 'authority_manage_administrator',
      component: AuthorityManageAdministrator,
    }, {
      path: RouterMapper.GetPath('pluginManage'),
      name: 'plugin_manage',
      component: PluginManage,
    }, {
      path: RouterMapper.GetPath('pluginUpgrade'),
      name: 'plugin_upgrade',
      component: PluginUpgrade,
    }, {
      path: RouterMapper.GetPath('agentManage'),
      name: 'agent_manage',
      component: AgentManage,
    }, {
      path: RouterMapper.GetPath('tokenManage'),
      name: 'token_manage',
      component: TokenManage,
    }, {
      path: RouterMapper.GetPath('permissionManage'),
      component: PermissionManage,
      children: [
        { path: '', redirect: 'user_manage' },
        { name: 'user_manage', path: RouterMapper.GetPath('userManage'), component: UserManage },
      ],
    },
  ],
});
