package org.prules.operator.learner.tools

double[] tab = [3,2, 1, 5, 4, 6, 1];
bins = 3;
res = PRulesUtil.discretizeFastEqFrequency(tab,bins)
println(res)
List[] desiredResults = [[2, 6], [1, 0], [4, 3, 5]]
i = 0;
for(List l : res ){
    assert l.equals(desiredResults[i])
    i++;
}