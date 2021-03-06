package ro.nextreports.server.web.action.analysis;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.Analysis;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Link;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.service.AnalysisService;
import ro.nextreports.server.service.SecurityService;
import ro.nextreports.server.util.PermissionUtil;
import ro.nextreports.server.web.NextServerSession;
import ro.nextreports.server.web.analysis.AnalysisBrowserPanel;
import ro.nextreports.server.web.analysis.AnalysisSection;
import ro.nextreports.server.web.analysis.model.SelectedAnalysisModel;
import ro.nextreports.server.web.common.menu.MenuPanel;
import ro.nextreports.server.web.common.misc.AjaxConfirmLink;
import ro.nextreports.server.web.core.section.SectionContext;
import ro.nextreports.server.web.core.section.SectionContextConstants;
import ro.nextreports.server.web.security.SecurityUtil;

public class DeleteActionLink extends AjaxConfirmLink {
	
	private AnalysisActionContext actionContext;
	
	@SpringBean
    private AnalysisService analysisService;
	
	@SpringBean
    private SecurityService securityService;
		
    public void setAnalysisService(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }        
	
	public DeleteActionLink(AnalysisActionContext actionContext) {
		super(MenuPanel.LINK_ID, new StringResourceModel("AnalysisPopupMenuModel.deleteAsk", null, 
					new Object[] { actionContext.getEntity().getName() }).getString());
		this.actionContext = actionContext;
		Injector.get().inject(this);
	}

	public void executeAction(AjaxRequestTarget target) {
		Entity entity = actionContext.getEntity();
		String id = getAnalysisId();
        try {
        	analysisService.removeAnalysis(id);
		} catch (NotFoundException e) {
			// TODO
			e.printStackTrace();						
		}

        if (id.equals(getSelectedAnalysisId())) {
            SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);
            List<Analysis> list = analysisService.getMyAnalysis();
            if (list.size() > 0) {            		
            	String _id = list.get(0).getId();
            	sectionContext.getData().put(SectionContextConstants.SELECTED_ANALYSIS_ID, _id);
            }
        }

        AnalysisBrowserPanel panel = findParent(AnalysisBrowserPanel.class);
        panel.getAnalysisPanel().changeDataProvider(new SelectedAnalysisModel(), target);
        target.add(panel);   
	}		
	
	@Override
	public boolean isVisible() {
		if (!SecurityUtil.hasPermission(securityService, PermissionUtil.getDelete(), getAnalysisId())) {
			return false;
		}		
		return true;
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		executeAction(target);		
	}

	private String getSelectedAnalysisId() {
        SectionContext sectionContext = NextServerSession.get().getSectionContext(AnalysisSection.ID);
        return sectionContext.getData().getString(SectionContextConstants.SELECTED_ANALYSIS_ID);
    }
	
	private String getAnalysisId() {
		Entity entity = actionContext.getEntity();
		String id;
		if (entity instanceof Link) {			 
		    id = ((Link)entity).getReference();       
		} else {
			id = entity.getId();
		}
		return id;
	}

}
