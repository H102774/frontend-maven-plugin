package com.github.eirslett.maven.plugins.frontend.mojo;

import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.InstallationException;
import com.github.eirslett.maven.plugins.frontend.lib.NodeAndNPMInstaller;
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mojo(name = "install-node-and-npm", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public final class InstallNodeAndNpmMojo extends AbstractFrontendMojo {

	private static final Logger LOGGER = LoggerFactory.getLogger(InstallNodeAndNpmMojo.class);

	/**
	 * Where to download Node.js binary from. Defaults to http://nodejs.org/dist/
	 */
	@Parameter(property = "nodeDownloadRoot", required = false, defaultValue = NodeAndNPMInstaller.DEFAULT_NODEJS_DOWNLOAD_ROOT)
	private String nodeDownloadRoot;

	/**
	 * Where to download NPM binary from. Defaults to http://registry.npmjs.org/npm/-/
	 */
	@Parameter(property = "npmDownloadRoot", required = false, defaultValue = NodeAndNPMInstaller.DEFAULT_NPM_DOWNLOAD_ROOT)
	private String npmDownloadRoot;

	/**
	 * Where to download Node.js and NPM binaries from.
	 *
	 * @deprecated use {@link #nodeDownloadRoot} and {@link #npmDownloadRoot} instead, this configuration will be used only when no {@link #nodeDownloadRoot} or {@link #npmDownloadRoot} is specified.
	 */
	@Parameter(property = "downloadRoot", required = false, defaultValue = "")
	@Deprecated
	private String downloadRoot;

	@Parameter(property = "nodeServer", required = false, defaultValue = "")
	private String nodeServer;

	@Parameter(property = "username", required = false, defaultValue = "")
	private String username;

	@Parameter(property = "password", required = false, defaultValue = "")
	private String password;

	/**
	 * The version of Node.js to install. IMPORTANT! Most Node.js version names start with 'v', for example 'v0.10.18'
	 */
	@Parameter(property = "nodeVersion", required = true)
	private String nodeVersion;

	/**
	 * The version of NPM to install.
	 */
	@Parameter(property = "npmVersion", required = true)
	private String npmVersion;

	@Parameter(property = "session", defaultValue = "${session}", readonly = true)
	private MavenSession session;

	/**
	 * Skips execution of this mojo.
	 */
	@Parameter(property = "skip.installnodenpm", defaultValue = "false")
	private Boolean skip;

	@Component(role = SettingsDecrypter.class)
	private SettingsDecrypter decrypter;

	@Parameter(property = "node.dist.prefix", defaultValue = "")
	private String nodeDistPrefix;

	@Parameter(property = "npm.dist.prefix", defaultValue = "")
	private String npmDistPrefix;

	@Override
	protected boolean skipExecution() {
		return this.skip;
	}

	@Override
	public void execute(FrontendPluginFactory factory) throws InstallationException {

		ProxyConfig proxyConfig = MojoUtils.getProxyConfig(session, decrypter);
		String nodeDownloadRoot = getNodeDownloadRoot();
		String npmDownloadRoot = getNpmDownloadRoot();

		if ((username == null || username.length() == 0) && (nodeServer != null && nodeServer.length() > 0)) {
			Server server = session.getSettings().getServer(nodeServer);
			if (server != null) {
				username = server.getUsername();
				password = server.getPassword();
			}
		}

		LOGGER.info("nodeServer=" + nodeServer);
		LOGGER.info("username=" + username);

		if (username != null && username.length() > 0) {
			factory.getAuthenticatingNodeAndNPMInstaller(username, password).install(nodeVersion, npmVersion, nodeDownloadRoot, npmDownloadRoot);
		} else {
			factory.getNodeAndNPMInstaller(proxyConfig).install(nodeVersion, npmVersion, nodeDownloadRoot, npmDownloadRoot);
		}
	}

	private String getNodeDownloadRoot() {
		if (downloadRoot != null && !"".equals(downloadRoot) && NodeAndNPMInstaller.DEFAULT_NODEJS_DOWNLOAD_ROOT.equals(nodeDownloadRoot)) {
			return downloadRoot;
		}
		return nodeDownloadRoot;
	}

	private String getNpmDownloadRoot() {
		if (downloadRoot != null && !"".equals(downloadRoot) && NodeAndNPMInstaller.DEFAULT_NPM_DOWNLOAD_ROOT.equals(npmDownloadRoot)) {
			return downloadRoot;
		}
		return npmDownloadRoot;
	}
}

