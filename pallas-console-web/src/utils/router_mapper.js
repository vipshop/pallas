const routermapperlist = [
    { name: 'login', path: '/login' },
    { name: 'indexManage', path: '/index_manage' },
    { name: 'indexDetail', path: '/index_detail' },
    { name: 'versionManage', path: '/index_detail/version_manage' },
    { name: 'templateManage', path: '/index_detail/template_manage' },
    { name: 'dynamicManage', path: '/index_detail/dynamic_manage' },
    { name: 'flowRecord', path: '/index_detail/flow_record' },
    { name: 'cronDelete', path: '/index_detail/cron_delete' },
    { name: 'routeManage', path: '/index_detail/route_manage' },
    { name: 'serviceManage', path: '/index_detail/service_manage' },
    { name: 'clusterManage', path: '/cluster_manage' },
    { name: 'clusterRouteManage', path: '/cluster_route_manage' },
    { name: 'clusterNodeRestart', path: '/cluster_node_restart' },
    { name: 'clusterDetail', path: '/cluster_detail' },
    { name: 'authorityManage', path: '/authority_manage' },
    { name: 'authorityManageAdministrator', path: '/authority_manage_administrator' },
    { name: 'pluginManage', path: '/plugin_manage' },
    { name: 'pluginUpgrade', path: '/plugin_manage/plugin_upgrade' },
    { name: 'agentManage', path: '/agent_manage' },
    { name: 'tokenManage', path: '/token_manage' },
    { name: 'permissionManage', path: '/permission_manage' },
    { name: 'userManage', path: '/permission_manage/user_manage' },
];

export default {
  GetPath(name) {
    const items = routermapperlist.filter(x => x.name === name);
    if (items !== undefined) {
      return items[0].path;
    }
    throw new Error('can not find route path ');
  },
};
