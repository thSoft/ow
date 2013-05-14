package net.vtst.ow.eclipse.less.linking;

import java.util.List;

import net.vtst.ow.eclipse.less.less.HashOrClassRef;
import net.vtst.ow.eclipse.less.less.HashOrClassRefTarget;
import net.vtst.ow.eclipse.less.less.LessPackage;
import net.vtst.ow.eclipse.less.less.LessUtils;
import net.vtst.ow.eclipse.less.less.Mixin;
import net.vtst.ow.eclipse.less.less.MixinUtils;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.linking.impl.IllegalNodeException;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.IEObjectDescription;

import com.google.inject.Inject;
// import org.eclipse.xtext.util.internal.Stopwatches;
// import org.eclipse.xtext.util.internal.Stopwatches.StoppedTask;

public class LessLinkingService extends DefaultLinkingService {
  
  @Inject
  private IQualifiedNameConverter qualifiedNameConverter;
  
  @Inject
  private LessMixinLinkingHelper mixinLinkingHelper;
  
  protected IEObjectDescription getBestMatchForHashOrClassRef(HashOrClassRef context, Iterable<IEObjectDescription> eObjectDescriptions) {
    EObject mixinCall = LessUtils.getNthAncestor(context, 2);
    if (mixinCall instanceof Mixin) {
      MixinUtils.Helper mixinHelper = MixinUtils.newHelper((Mixin) mixinCall);
      for (IEObjectDescription eObjectDescription : eObjectDescriptions) {
        EObject eObject = eObjectDescription.getEObjectOrProxy();
        if (eObject instanceof HashOrClassRefTarget) {
          LessMixinLinkingHelper.Prototype prototype = 
              mixinLinkingHelper.getPrototypeForMixinDefinition((HashOrClassRefTarget) eObject);
          if (prototype.checkMixinCall(mixinHelper, null))
            return eObjectDescription;
        }
      }
    }
    return getBestMatchDefault(context, eObjectDescriptions);
  }

  protected IEObjectDescription getBestMatchDefault(EObject context, Iterable<IEObjectDescription> eObjectDescriptions) {
    for (IEObjectDescription eObjectDescription : eObjectDescriptions) {
      return eObjectDescription;
    }
    return null;
  }

  protected IEObjectDescription getBestMatch(EObject context, Iterable<IEObjectDescription> eObjectDescriptions) {
    if (context instanceof HashOrClassRef) {
      return getBestMatchForHashOrClassRef((HashOrClassRef) context, eObjectDescriptions);
    } else {
      return getBestMatchDefault(context, eObjectDescriptions);
    }
  }

  public List<EObject> getLinkedObjects(EObject context, EReference ref, INode node)
      throws IllegalNodeException {
    if (LessPackage.eINSTANCE.getHashOrClassRefTarget().equals(ref.getEReferenceType())) {
      return mixinLinkingHelper.getLinkedObjects(context, ref, node);
    } else {
      return super.getLinkedObjects(context, ref, node);
    }
  }

}
