package gg.xp

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.views.View
import jakarta.inject.Inject
import groovy.transform.CompileStatic

@Controller
@ExecuteOn(TaskExecutors.VIRTUAL)
@CompileStatic
class TipController {

    @Inject
    TipService tipService
    
    @Inject
    SettingsService settingsService

    @Get("/")
    @View("index")
    Map<String, Object> index() {
        return ([
            tips: tipService.topTippers,
            tokenConfigured: settingsService.isTokenConfigured()
        ] as Map<String, Object>)
    }

    @Get("/tips/table")
    @View("fragments/tips-table")
    Map<String, Object> tipsTable() {
        return ([
            tips: tipService.topTippers,
            tokenConfigured: settingsService.isTokenConfigured()
        ] as Map<String, Object>)
    }
}
