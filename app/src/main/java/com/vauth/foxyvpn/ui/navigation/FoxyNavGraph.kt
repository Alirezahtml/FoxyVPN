package com.vauth.foxyvpn.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vauth.foxyvpn.FoxyVpnApp
import com.vauth.foxyvpn.ui.screens.AccountScreen
import com.vauth.foxyvpn.ui.screens.HomeScreen
import com.vauth.foxyvpn.ui.screens.LoginScreen
import com.vauth.foxyvpn.ui.screens.LogsScreen
import com.vauth.foxyvpn.ui.screens.ServerListScreen
import com.vauth.foxyvpn.ui.screens.SettingsScreen
import com.vauth.foxyvpn.ui.screens.SplashScreen
import com.vauth.foxyvpn.ui.theme.ThemeController

object FoxyRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val SERVERS = "servers"
    const val SETTINGS = "settings"
    const val ACCOUNT = "account"
    const val LOGS = "logs"
}

@Composable
fun FoxyNavGraph(
    navController: NavHostController = rememberNavController(),
    app: FoxyVpnApp,
    themeController: ThemeController,
    onRequestConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = FoxyRoutes.SPLASH,
    ) {
        composable(
            route = FoxyRoutes.SPLASH,
            exitTransition = { fadeOut(animationSpec = tween(300)) },
        ) {
            SplashScreen(
                authRepository = app.authRepository,
                onSignedIn = {
                    navController.navigate(FoxyRoutes.HOME) { popUpTo(FoxyRoutes.SPLASH) { inclusive = true } }
                },
                onNeedsLogin = {
                    navController.navigate(FoxyRoutes.LOGIN) { popUpTo(FoxyRoutes.SPLASH) { inclusive = true } }
                },
            )
        }

        composable(
            route = FoxyRoutes.LOGIN,
            enterTransition = { fadeIn(animationSpec = tween(350)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
        ) {
            LoginScreen(
                authRepository = app.authRepository,
                onSignedIn = {
                    navController.navigate(FoxyRoutes.HOME) { popUpTo(FoxyRoutes.LOGIN) { inclusive = true } }
                },
            )
        }

        composable(
            route = FoxyRoutes.HOME,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
        ) {
            HomeScreen(
                app = app,
                themeController = themeController,
                onRequestConnect = onRequestConnect,
                onDisconnect = onDisconnect,
                onOpenServers = { navController.navigate(FoxyRoutes.SERVERS) },
                onOpenSettings = { navController.navigate(FoxyRoutes.SETTINGS) },
                onOpenAccount = { navController.navigate(FoxyRoutes.ACCOUNT) },
                onOpenLogin = { navController.navigate(FoxyRoutes.LOGIN) },
            )
        }

        // iOS-style slide up from bottom modal presentations
        composable(
            route = FoxyRoutes.SERVERS,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeIn(animationSpec = tween(200))
            },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeOut(animationSpec = tween(150))
            },
        ) {
            ServerListScreen(
                serverListClient = app.serverListClient,
                proxyStateStore = app.proxyStateStore,
                onServerSelected = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = FoxyRoutes.SETTINGS,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeIn(animationSpec = tween(200))
            },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeOut(animationSpec = tween(150))
            },
        ) {
            SettingsScreen(
                settingsStore = app.settingsStore,
                onOpenLogs = { navController.navigate(FoxyRoutes.LOGS) },
                onOpenAccount = { navController.navigate(FoxyRoutes.ACCOUNT) },
                onSignOut = {
                    onDisconnect()
                    app.tokenStore.clear()
                    navController.navigate(FoxyRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = FoxyRoutes.ACCOUNT,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeIn(animationSpec = tween(200))
            },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeOut(animationSpec = tween(150))
            },
        ) {
            AccountScreen(
                authRepository = app.authRepository,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = FoxyRoutes.LOGS,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeIn(animationSpec = tween(200))
            },
            exitTransition = { fadeOut(animationSpec = tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = tween(150)) },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeOut(animationSpec = tween(150))
            },
        ) {
            LogsScreen(onBack = { navController.popBackStack() })
        }
    }
}
